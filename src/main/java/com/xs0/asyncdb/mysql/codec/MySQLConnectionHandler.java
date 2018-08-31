package com.xs0.asyncdb.mysql.codec;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.xs0.asyncdb.common.Configuration;
import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.common.util.FutureUtils;
import com.xs0.asyncdb.mysql.MySQLConnection;
import com.xs0.asyncdb.mysql.codec.commands.ExecuteQueryCommand;
import com.xs0.asyncdb.mysql.codec.commands.MySQLCommand;
import com.xs0.asyncdb.mysql.codec.statemachine.InitialHandshakeStateMachine;
import com.xs0.asyncdb.mysql.codec.statemachine.MySQLStateMachine;
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

public class MySQLConnectionHandler extends SimpleChannelInboundHandler<Object> {
    private static final int STATE_HANDSHAKING = 0;
    private static final int STATE_RUNNING_STATE_MACHINE = 1;
    private static final int STATE_IDLE = 2;
    private static final int STATE_CLOSED = 3;



    public final Configuration configuration;
    private final MySQLConnection handlerDelegate;
    private final EventLoopGroup group;

    private final Logger log;
    private final Bootstrap bootstrap;

    private final SendLongDataEncoder sendLongDataEncoder = SendLongDataEncoder.instance();
    public final DecoderRegistry decoderRegistry;

    private ChannelHandlerContext currentContext = null;

    private HandshakeMessage serverInfo;

    private int state;
    private MySQLStateMachine currentStateMachine;
    private CompletableFuture<?> currentPromise;
    private boolean connectCalled;
    private boolean connectFinished;
    private final ArrayDeque<MySQLCommand> commandQueue = new ArrayDeque<>();

    public MySQLConnectionHandler(Configuration configuration,
                                  MySQLConnection handlerDelegate,
                                  EventLoopGroup group,
                                  String connectionId) {

        this.configuration = configuration;
        this.handlerDelegate = handlerDelegate;
        this.group = group;

        this.log = LoggerFactory.getLogger("[connection-handler]" + connectionId);
        this.bootstrap = new Bootstrap().group(this.group);
        this.decoderRegistry = new DecoderRegistry(configuration.charset);
    }

    public CompletableFuture<MySQLConnectionHandler> connect() {
        if (state == STATE_CLOSED)
            return FutureUtils.failedFuture(new IllegalStateException("This connection has already been closed"));

        if (connectFinished)
            return CompletableFuture.completedFuture(this);

        if (connectCalled)
            return (CompletableFuture<MySQLConnectionHandler>) currentPromise;

        connectCalled = true;
        CompletableFuture<MySQLConnectionHandler> promise = new CompletableFuture<>();
        this.currentPromise = promise;
        this.currentStateMachine = new InitialHandshakeStateMachine();
        this.state = STATE_RUNNING_STATE_MACHINE;

        currentStateMachine.init(this);

        this.bootstrap.channel(NioSocketChannel.class);

        this.bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(
                    new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 0xFFFFFF + 4, 0, 3, 1, 4, true),
                    MySQLOneToOneEncoder.instance(),
                    sendLongDataEncoder,
                    MySQLConnectionHandler.this
                );
            }
        });

        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture connectFuture = this.bootstrap.connect(new InetSocketAddress(configuration.host, configuration.port));
        connectFuture.addListener(f -> {
            if (f.isSuccess()) {
                // nothing?
            } else {
                promise.completeExceptionally(f.cause());
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

        MySQLStateMachine.Result result;

        if (currentStateMachine != null) {
            try {
                result = currentStateMachine.processPacket(packet);
                if (result == null) {
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
                if (state == STATE_HANDSHAKING) {
                    connectFinished = true;
                    ((CompletableFuture<MySQLConnectionHandler>)currentPromise).complete(this);
                }

                MySQLCommand next = commandQueue.pollFirst();
                if (next == null) {
                    state = STATE_IDLE;
                    currentStateMachine = null;
                    currentPromise = null;
                } else {
                    state = STATE_RUNNING_STATE_MACHINE;
                    currentStateMachine = next.createStateMachine();
                    currentPromise = next.getPromise();

                    MySQLStateMachine.Result initResult;
                    try {
                        initResult = currentStateMachine.init(this);
                    } catch (Exception e) {
                        initResult = MySQLStateMachine.Result.protocolErrorAbortEverything(e);
                    }

                    handleStateMachineResult(initResult);
                }
                break;

            case PROTOCOL_ERROR_ABORT_ABORT_ABORT:
                state = STATE_CLOSED;

                currentPromise.completeExceptionally(result.error);
                currentPromise = null;
                currentStateMachine = null;

                for (MySQLCommand command : commandQueue)
                    command.getPromise().completeExceptionally(result.error);

                disconnect();
                break;
        }
    }

    void enqueueCommand(MySQLCommand command) {
        commandQueue.addLast(command);

        if (state == STATE_IDLE)
            handleStateMachineResult(MySQLStateMachine.Result.stateMachineFinished());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel became active");
        handlerDelegate.connected(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel became inactive");
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

    public ChannelFuture disconnect() {
        return this.currentContext.close();
    }

    public boolean isConnected() {
        if (this.currentContext != null && this.currentContext.channel() != null) {
            return this.currentContext.channel().isActive();
        } else {
            return false;
        }
    }

    /*
    ChannelFuture executePreparedStatement(byte[] statementId, int columnsCount, List<Object> values, List<ColumnDefinitionMessage> parameters) {
        decoder.preparedStatementExecuteStarted(columnsCount, parameters.size());
        this.currentColumns.clear();
        this.currentParameters.clear();

        HashSet<Integer> nonLongIndices = new HashSet<>();

        int index = 0;
        for (Object value : values) {
            if (!isLong(value)) {
                nonLongIndices.add(index);
            }
            index++;
        }

        // quick case - all values are non-long, so we just send them along
        if (nonLongIndices.size() == values.size())
            return writeAndHandleError(new PreparedStatementExecuteMessage(statementId, values, nonLongIndices, parameters));

        // long case - we send the parameters one by one, finally resolving this promise after everything is done
        ChannelPromise promise = currentContext.newPromise();
        continueSendingLongParameters(statementId, values, parameters, 0, promise, nonLongIndices);
        return promise;
    }

    void continueSendingLongParameters(byte[] statementId, List<Object> values, List<ColumnDefinitionMessage> parameters, int currentIndex, ChannelPromise finalPromise, HashSet<Integer> nonLongIndices) {
        while (currentIndex < values.size() && nonLongIndices.contains(currentIndex))
            currentIndex++;

        if (currentIndex < values.size()) {
            // send next long value and call back here

            int nextIndex = currentIndex + 1;
            ChannelFuture future = sendLongParameter(statementId, currentIndex, values.get(currentIndex));
            future.addListener(f -> {
                if (f.isSuccess()) {
                    continueSendingLongParameters(statementId, values, parameters, nextIndex, finalPromise, nonLongIndices);
                } else
                if (f.isCancelled()) {
                    finalPromise.cancel(false);
                } else {
                    finalPromise.tryFailure(f.cause());
                }
            });
        } else {
            // we're done

            ChannelFuture future = writeAndHandleError(new PreparedStatementExecuteMessage(statementId, values, nonLongIndices, parameters));
            future.addListener(f -> {
                if (f.isSuccess()) {
                    finalPromise.trySuccess();
                } else
                if (f.isCancelled()) {
                    finalPromise.cancel(false);
                } else {
                    finalPromise.tryFailure(f.cause());
                }
            });
        }
    }

    boolean isLong(Object value) {
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            return bytes.length > SendLongDataEncoder.LONG_THRESHOLD;
        }
        if (value instanceof ByteBuf) {
            ByteBuf bytes = (ByteBuf) value;
            return bytes.readableBytes() > SendLongDataEncoder.LONG_THRESHOLD;
        }
        if (value instanceof ByteBuffer) {
            ByteBuffer bytes = (ByteBuffer) value;
            return bytes.remaining() > SendLongDataEncoder.LONG_THRESHOLD;
        }

        return false;
    }

    private ChannelFuture sendLongParameter(byte[] statementId, int index, Object longValue) {
        ByteBuf buffer;

        if (longValue instanceof ByteBuf) {
            buffer = (ByteBuf) longValue;
        } else
        if (longValue instanceof byte[]) {
            buffer = Unpooled.wrappedBuffer((byte[]) longValue);
        } else
        if (longValue instanceof ByteBuffer) {
            buffer = Unpooled.wrappedBuffer((ByteBuffer) longValue);
        } else {
            throw new IllegalStateException("Unknown long parameter type"); // see isLong
        }

        return writeAndHandleError(new SendLongDataMessage(statementId, buffer, index));
    }

    private void onPreparedStatementPrepareResponse(PreparedStatementPrepareResponse message) {
        this.currentPreparedStatementHolder = new PreparedStatementHolder(this.currentPreparedStatement.statement, message);
    }

    void onColumnDefinitionFinished() {
        ArrayList<ColumnDefinitionMessage> columns;

        if (this.currentPreparedStatementHolder != null) {
            columns = this.currentPreparedStatementHolder.columns;
        } else {
            columns = this.currentColumns;
        }

        this.currentQuery = new MutableResultSet<>(columns);

        if (this.currentPreparedStatementHolder != null) {
            this.parsedStatements.put(this.currentPreparedStatementHolder.statement, this.currentPreparedStatementHolder);

            this.executePreparedStatement(
                this.currentPreparedStatementHolder.statementId(),
                this.currentPreparedStatementHolder.columns.size(),
                this.currentPreparedStatement.values,
                this.currentPreparedStatementHolder.parameters
            );

            this.currentPreparedStatementHolder = null;
            this.currentPreparedStatement = null;
        }
    }

    private ChannelFuture writeAndHandleError(Object message) {
        if (this.currentContext.channel().isActive()) {
            ChannelFuture res = this.currentContext.writeAndFlush(message);

            res.addListener(future -> {
                if (!future.isSuccess()) {
                    handleException(future.cause());
                }
            });

            return res;
        } else {
            DatabaseException error = new DatabaseException("This channel is not active and can't take messages");
            handleException(error);
            return this.currentContext.channel().newFailedFuture(error);
        }
    }

    void handleEOF(ServerMessage m) {
        if (m instanceof EOFMessage) {
            EOFMessage eof = (EOFMessage) m;

            MutableResultSet<ColumnDefinitionMessage> resultSet = this.currentQuery;
            this.clearQueryState();

            if (resultSet != null) {
                handlerDelegate.onResultSet(resultSet, eof);
            } else {
                handlerDelegate.onEOF(eof);
            }
        } else
        if (m instanceof AuthenticationSwitchRequest) {
            AuthenticationSwitchRequest authenticationSwitch = (AuthenticationSwitchRequest) m;
            handlerDelegate.switchAuthentication(authenticationSwitch);
        }
    }
    */

    void schedule(Runnable block, Duration duration) {
        this.currentContext.channel().eventLoop().schedule(block, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void sendMessage(ClientMessage message) {
        switch (this.state) {
            case STATE_HANDSHAKING:
            case STATE_RUNNING_STATE_MACHINE:
                this.currentContext.write(message);
                break;

            case STATE_IDLE:
                throw new IllegalStateException("Sending a message on an idle channel");

            case STATE_CLOSED:
                throw new IllegalStateException("Sending a message on a closed channel");
        }
    }


    public void setServerInfo(HandshakeMessage serverInfo) {
        this.serverInfo = serverInfo;
    }

    public HandshakeMessage serverInfo() {
        return serverInfo;
    }

    CompletableFuture<QueryResult> executePreparedStatement(PreparedStatementInfo psInfo, List<Object> values) {
        throw new UnsupportedOperationException("TODO");
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
}