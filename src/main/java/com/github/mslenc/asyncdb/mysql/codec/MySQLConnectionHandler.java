package com.github.mslenc.asyncdb.mysql.codec;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.github.mslenc.asyncdb.mysql.message.client.ClientMessage;
import com.github.mslenc.asyncdb.mysql.message.server.ErrorMessage;
import com.github.mslenc.asyncdb.mysql.message.server.HandshakeMessage;
import com.github.mslenc.asyncdb.mysql.state.commands.*;
import com.github.mslenc.asyncdb.common.Configuration;
import com.github.mslenc.asyncdb.common.PreparedStatement;
import com.github.mslenc.asyncdb.common.QueryResult;
import com.github.mslenc.asyncdb.common.exceptions.ConnectionClosedException;
import com.github.mslenc.asyncdb.common.exceptions.DatabaseException;
import com.github.mslenc.asyncdb.common.util.BufferDumper;
import com.github.mslenc.asyncdb.common.util.FutureUtils;
import com.github.mslenc.asyncdb.mysql.binary.BinaryRowEncoder;
import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import com.github.mslenc.asyncdb.mysql.decoder.ErrorDecoder;
import com.github.mslenc.asyncdb.mysql.ex.MySQLException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.mslenc.asyncdb.common.util.FutureUtils.failedFuture;
import static com.github.mslenc.asyncdb.common.util.FutureUtils.safelyComplete;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;

public class MySQLConnectionHandler extends SimpleChannelInboundHandler<Object> implements MySQLCommand.Support {
    private static final int STATE_AWAITING_CONNECT_CALL = 0;
    private static final int STATE_AWAITING_SOCKET = 1;
    private static final int STATE_RUNNING_STATE_MACHINE = 2;
    private static final int STATE_IDLE = 3;
    private static final int STATE_CLOSED = 4;

    private final Configuration configuration;
    private final EventLoopGroup group;
    private final Logger log;
    private final DecoderRegistry decoderRegistry;

    private ChannelHandlerContext currentContext = null;

    private HandshakeMessage serverInfo;

    private int state;

    private InitialHandshakeCommand initialHandshake;
    private MySQLCommand currentCommand;
    private final ArrayDeque<MySQLCommand> commandQueue = new ArrayDeque<>();
    public final CodecSettings codecSettings = new CodecSettings(ZoneId.systemDefault(), UTC);

    public MySQLConnectionHandler(Configuration configuration,
                                  EventLoopGroup group,
                                  String connectionId) {

        super(Object.class);

        this.configuration = configuration;
        this.group = group;
        this.log = LoggerFactory.getLogger("[connection-handler]" + connectionId);
        this.decoderRegistry = DecoderRegistry.instance();
    }

    public CompletableFuture<MySQLConnectionHandler> connect() {
        if (state != STATE_AWAITING_CONNECT_CALL)
            return failedFuture(new DatabaseException("connect() was already called"));

        state = STATE_AWAITING_SOCKET;

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(
                    new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 0xFFFFFF + 4, 0, 3, 1, 4, true),
                    new LongPacketMerger(),

                    ClientMessageEncoder.instance(),
                    MySQLConnectionHandler.this
                );
            }
        });

        CompletableFuture<MySQLConnectionHandler> promise = new CompletableFuture<>();

        initialHandshake = new InitialHandshakeCommand(this, configuration, promise);

        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(configuration.host, configuration.port));
        connectFuture.addListener(f -> {
            if (f.isSuccess()) {
                state = STATE_RUNNING_STATE_MACHINE;
                currentCommand = initialHandshake;
                handleStateMachineResult(initialHandshake.start(this));
            } else {
                safelyFail(initialHandshake, f.cause());
            }
        });

        return promise;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object message) {
        ByteBuf packet;
        if (message instanceof ByteBuf) {
            packet = (ByteBuf) message;
        } else {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Received a message: \n{}", BufferDumper.dumpAsHex(packet));
        }

        MySQLCommand.Result result;

        if (currentCommand != null) {
            try {
                result = currentCommand.processPacket(packet, this);
                if (result != null) {
                    this.currentContext.flush();
                } else {
                    result = MySQLCommand.Result.protocolErrorAbortEverything("State machine returned no result");
                }
            } catch (Exception e) {
                result = MySQLCommand.Result.protocolErrorAbortEverything(e); // who knows what state it is in now..
            }
        } else {
            result = MySQLCommand.Result.protocolErrorAbortEverything(new IllegalStateException("A message received with no state machine active"));
        }

        handleStateMachineResult(result);
    }

    private void handleStateMachineResult(MySQLCommand.Result result) {
        switch (result.resultType) {
            case EXPECTING_MORE_PACKETS:
                // nothing to handle here :)
                break;

            case STATE_MACHINE_FINISHED:
                MySQLCommand next = commandQueue.pollFirst();
                if (next == null) {
                    state = STATE_IDLE;
                    currentCommand = null;
                } else {
                    state = STATE_RUNNING_STATE_MACHINE;
                    currentCommand = next;

                    MySQLCommand.Result initResult;
                    try {
                        initResult = currentCommand.start(this);
                        if (initResult != null) {
                            currentContext.flush();
                        } else {
                            initResult = MySQLCommand.Result.protocolErrorAbortEverything("State machine returned no result");
                        }
                    } catch (Exception e) {
                        initResult = MySQLCommand.Result.protocolErrorAbortEverything(e);
                    }

                    handleStateMachineResult(initResult);
                }
                break;

            case PROTOCOL_ERROR_ABORT_ABORT_ABORT:
                doCloseCleanUp(result.error);
                break;

            case DISCONNECT:
                doCloseCleanUp(null);
                safelyComplete(result.promise, null);
                break;
        }
    }

    void doCloseCleanUp(Throwable error) {
        try {
            if (error != null) {
                safelyFail(currentCommand, error);
            } else {
                safelyCompleteWithNull(currentCommand); // we assume it's the promise for close()
            }

            for (MySQLCommand command : commandQueue)
                safelyFail(command, new ConnectionClosedException(error));
        } finally {
            currentCommand = null;
            state = STATE_CLOSED;
            commandQueue.clear();
            currentContext.disconnect();
        }
    }

    void resumeExecutionIfIdle() {
        if (state == STATE_IDLE)
            handleStateMachineResult(MySQLCommand.Result.stateMachineFinished());
    }

    void enqueueCommand(MySQLCommand command) {
        commandQueue.addLast(command);
        resumeExecutionIfIdle();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Channel became active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Channel became inactive");
        doCloseCleanUp(new ConnectionClosedException());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CodecException) {
            handleException(cause.getCause());
        } else {
            handleException(cause);
        }
    }

    private void handleException(Throwable cause) {
        handleStateMachineResult(MySQLCommand.Result.protocolErrorAbortEverything(cause));

//        handlerDelegate.exceptionCaught(cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.currentContext = ctx;
    }

    public CompletableFuture<Void> disconnect() {
        if (state == STATE_CLOSED)
            return failedFuture(new ConnectionClosedException());

        CompletableFuture<Void> promise = new CompletableFuture<>();
        enqueueCommand(new DisconnectCommand(promise));
        return promise;
    }

    public boolean isConnected() {
        if (this.currentContext != null && this.currentContext.channel() != null) {
            return this.currentContext.channel().isActive();
        } else {
            return false;
        }
    }

    void schedule(Runnable block, Duration duration) {
        this.currentContext.channel().eventLoop().schedule(block, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void sendMessage(ClientMessage message) {
        switch (this.state) {
            case STATE_RUNNING_STATE_MACHINE:
                if (log.isTraceEnabled()) {
                    log.trace("Writing {}", message.toString(true));
                } else {
                    log.debug("Writing {}", message);
                }

                this.currentContext.write(message);
                break;

            case STATE_IDLE:
                throw new IllegalStateException("Sending a message on an idle channel");

            case STATE_CLOSED:
                throw new IllegalStateException("Sending a message on a closed channel");

            case STATE_AWAITING_CONNECT_CALL:
                throw new IllegalStateException("Sending a message on a channel that wasn't connect()ed yet");

            case STATE_AWAITING_SOCKET:
                throw new IllegalStateException("Sending a message on a channel that isn't connected yet");
        }
    }


    public void setServerInfo(HandshakeMessage serverInfo) {
        this.serverInfo = serverInfo;
    }

    public HandshakeMessage serverInfo() {
        return serverInfo;
    }

    CompletableFuture<QueryResult> executePreparedStatement(PreparedStatementInfo psInfo, List<Object> values) {
        if (values == null)
            values = Collections.emptyList();

        if (psInfo.paramDefs.size() != values.size())
            return FutureUtils.failedFuture(new MySQLException(new ErrorMessage(1058, "21S01", "Column count doesn't match value count")));

        CompletableFuture<QueryResult> promise = new CompletableFuture<>();
        enqueueCommand(new ExecutePreparedStatementCommand(psInfo, values, codecSettings, promise));
        return promise;
    }

    CompletableFuture<Void> closePreparedStatement(PreparedStatementInfo psInfo) {
        if (psInfo.wasClosed()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> promise = new CompletableFuture<>();
        enqueueCommand(new ClosePreparedStatementCommand(psInfo, promise));
        return promise.whenComplete((closeSuccess, closeError) -> {
            if (closeError == null) {
                psInfo.markAsClosed();
            }
        });
    }

    public CompletableFuture<QueryResult> sendQuery(ByteBuf queryUtf8) {
        CompletableFuture<QueryResult> promise = new CompletableFuture<>();
        enqueueCommand(new TextBasedQueryCommand(queryUtf8, promise, codecSettings));
        return promise;
    }

    public CompletableFuture<PreparedStatement> prepareStatement(String query) {
        CompletableFuture<PreparedStatement> promise = new CompletableFuture<>();
        enqueueCommand(new PrepareStatementCommand(query, promise));
        return promise;
    }

    @Override
    public ErrorMessage decodeErrorAfterHeader(ByteBuf packet) {
        return ErrorDecoder.decodeAfterHeader(packet, UTF_8, serverInfo.serverCapabilities);
    }

    @Override
    public DecoderRegistry decoderRegistry() {
        return decoderRegistry;
    }

    // TODO: move this somewhere else?
    private static final BinaryRowEncoder BINARY_ROW_ENCODER = new BinaryRowEncoder(UTF_8);
    @Override
    public BinaryRowEncoder getBinaryEncoders() {
        return BINARY_ROW_ENCODER;
    }

    @Override
    public PreparedStatement createPreparedStatement(String query, PreparedStatementInfo psInfo) {
        // TODO: remember them all, for clean-up?
        return new PreparedStatementImpl(this, query, psInfo);
    }

    private static void safelyFail(MySQLCommand command, Throwable cause) {
        if (command != null)
            FutureUtils.safelyFail(command.getPromise(), cause);
    }

    private static void safelyCompleteWithNull(MySQLCommand command) {
        if (command != null)
            FutureUtils.safelyComplete(command.getPromise(), null);
    }
}