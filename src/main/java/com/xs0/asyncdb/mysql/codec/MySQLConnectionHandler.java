package com.xs0.asyncdb.mysql.codec;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.xs0.asyncdb.common.Configuration;
import com.xs0.asyncdb.common.PreparedStatement;
import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.common.exceptions.ConnectionClosedException;
import com.xs0.asyncdb.common.exceptions.DatabaseException;
import com.xs0.asyncdb.common.util.BufferDumper;
import com.xs0.asyncdb.common.util.FutureUtils;
import com.xs0.asyncdb.mysql.binary.BinaryRowEncoder;
import com.xs0.asyncdb.mysql.codec.commands.*;
import com.xs0.asyncdb.mysql.codec.statemachine.InitialHandshakeStateMachine;
import com.xs0.asyncdb.mysql.codec.statemachine.MySQLStateMachine;
import com.xs0.asyncdb.mysql.decoder.ErrorDecoder;
import com.xs0.asyncdb.mysql.ex.MySQLException;
import com.xs0.asyncdb.mysql.message.client.*;
import com.xs0.asyncdb.mysql.message.server.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xs0.asyncdb.common.util.FutureUtils.failedFuture;
import static com.xs0.asyncdb.common.util.FutureUtils.safelyComplete;
import static com.xs0.asyncdb.common.util.FutureUtils.safelyFail;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MySQLConnectionHandler extends SimpleChannelInboundHandler<Object> implements MySQLStateMachine.Support {
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
    private int sequenceNumber = 1;

    private InitialHandshakeStateMachine initialHandshake;
    private MySQLStateMachine currentStateMachine;
    private CompletableFuture<?> currentPromise;
    private final ArrayDeque<MySQLCommand> commandQueue = new ArrayDeque<>();

    public MySQLConnectionHandler(Configuration configuration,
                                  EventLoopGroup group,
                                  String connectionId) {

        super(Object.class);

        this.configuration = configuration;
        this.group = group;
        this.log = LoggerFactory.getLogger("[connection-handler]" + connectionId);
        this.decoderRegistry = new DecoderRegistry(configuration.charset);
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

                    MySQLOneToOneEncoder.instance(),
                    MySQLConnectionHandler.this
                );
            }
        });

        CompletableFuture<MySQLConnectionHandler> promise = new CompletableFuture<>();

        initialHandshake = new InitialHandshakeStateMachine(this, configuration, promise);

        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(configuration.host, configuration.port));
        connectFuture.addListener(f -> {
            if (f.isSuccess()) {
                state = STATE_RUNNING_STATE_MACHINE;
                currentStateMachine = initialHandshake;
                currentPromise = initialHandshake.getPromise();
                handleStateMachineResult(initialHandshake.start(this));
            } else {
                safelyFail(promise, f.cause());
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

        MySQLStateMachine.Result result;

        if (currentStateMachine != null) {
            try {
                result = currentStateMachine.processPacket(packet, this);
                if (result != null) {
                    this.currentContext.flush();
                } else {
                    result = MySQLStateMachine.Result.protocolErrorAbortEverything("State machine returned no result");
                }
            } catch (Exception e) {
                result = MySQLStateMachine.Result.protocolErrorAbortEverything(e); // who knows what state it is in now..
            }
        } else {
            result = MySQLStateMachine.Result.protocolErrorAbortEverything(new IllegalStateException("A message received with no state machine active"));
        }

        handleStateMachineResult(result);
    }

    private void handleStateMachineResult(MySQLStateMachine.Result result) {
        switch (result.resultType) {
            case EXPECTING_MORE_PACKETS:
                // nothing to handle here :)
                break;

            case STATE_MACHINE_FINISHED:
                MySQLCommand next = commandQueue.pollFirst();
                if (next == null) {
                    state = STATE_IDLE;
                    currentStateMachine = null;
                    currentPromise = null;
                } else {
                    state = STATE_RUNNING_STATE_MACHINE;
                    sequenceNumber = 0; // a new command is supposed to have seq number 0
                    currentStateMachine = next.createStateMachine();
                    currentPromise = next.getPromise();

                    MySQLStateMachine.Result initResult;
                    try {
                        initResult = currentStateMachine.start(this);
                        if (initResult != null) {
                            currentContext.flush();
                        } else {
                            initResult = MySQLStateMachine.Result.protocolErrorAbortEverything("State machine returned no result");
                        }
                    } catch (Exception e) {
                        initResult = MySQLStateMachine.Result.protocolErrorAbortEverything(e);
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
                safelyFail(currentPromise, error);
            } else {
                safelyComplete(currentPromise, null); // we assume it's the promise for close()
            }

            for (MySQLCommand command : commandQueue)
                safelyFail(command.getPromise(), new ConnectionClosedException(error));
        } finally {
            currentPromise = null;
            currentStateMachine = null;
            state = STATE_CLOSED;
            disconnect();
            commandQueue.clear();
        }
    }

    void resumeExecutionIfIdle() {
        if (state == STATE_IDLE)
            handleStateMachineResult(MySQLStateMachine.Result.stateMachineFinished());
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
        handleStateMachineResult(MySQLStateMachine.Result.protocolErrorAbortEverything(cause));

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
                message.assignPacketSequenceNumber(sequenceNumber++);
                if (sequenceNumber > 255) {
                    sequenceNumber = 1; // 0 is reserved for initial packet in a command (if I read the manual correctly)
                }

                log.debug("Writing {}", message);

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
        enqueueCommand(new ExecutePreparedStatementCommand(psInfo, values, promise));
        return promise;
    }

    void closePreparedStatement(PreparedStatementInfo psInfo, CompletableFuture<Void> closingPromise) {
        throw new UnsupportedOperationException("TODO");
    }

    public com.xs0.asyncdb.common.PreparedStatement rememberPreparedStatement(PreparedStatementInfo info) {
        throw new UnsupportedOperationException("TODO");
    }

    public CompletableFuture<QueryResult> sendQuery(String query) {
        CompletableFuture<QueryResult> promise = new CompletableFuture<>();
        enqueueCommand(new ExecuteQueryCommand(query, promise));
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
}