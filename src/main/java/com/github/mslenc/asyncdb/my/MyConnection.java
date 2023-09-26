package com.github.mslenc.asyncdb.my;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.github.mslenc.asyncdb.ex.ConnectionTimeoutException;
import com.github.mslenc.asyncdb.ex.SqlServerException;
import com.github.mslenc.asyncdb.my.commands.*;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.my.io.ClientMessageEncoder;
import com.github.mslenc.asyncdb.my.io.LongPacketMerger;
import com.github.mslenc.asyncdb.my.io.PreparedStatementInfo;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilderFactory;
import com.github.mslenc.asyncdb.my.msgclient.ClientMessage;
import com.github.mslenc.asyncdb.my.msgserver.ErrorMessage;
import com.github.mslenc.asyncdb.my.msgserver.HandshakeMessage;
import com.github.mslenc.asyncdb.ex.ConnectionClosedException;
import com.github.mslenc.asyncdb.ex.DatabaseException;
import com.github.mslenc.asyncdb.util.BufferDumper;
import com.github.mslenc.asyncdb.util.FutureUtils;
import com.github.mslenc.asyncdb.util.SslHandlerProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;

import static com.github.mslenc.asyncdb.util.FutureUtils.failedFuture;
import static com.github.mslenc.asyncdb.util.FutureUtils.safelyComplete;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;


public class MyConnection extends SimpleChannelInboundHandler<Object> {
    private static final int STATE_AWAITING_CONNECT_CALL = 0;
    private static final int STATE_AWAITING_SOCKET = 1;
    private static final int STATE_RUNNING_STATE_MACHINE = 2;
    private static final int STATE_IDLE = 3;
    private static final int STATE_CLOSED = 4;

    private final EventLoopGroup group;
    private final Logger log;
    private final SocketAddress remoteAddress;
    private final Runnable onDisconnect;
    public final MyEncoders encoders;
    private final Duration queryTimeout;
    private final SslHandlerProvider sslContextProvider;

    private ChannelHandlerContext channel;

    private HandshakeMessage serverInfo;

    private int state;

    private MyHandshakeCmd initialHandshake;
    private MyCommand currentCommand;
    private final ArrayDeque<MyCommand> commandQueue = new ArrayDeque<>();

    public MyConnection(EventLoopGroup eventLoopGroup, SocketAddress remoteAddress, String connectionId, MyEncoders encoders, Runnable onDisconnect, Duration queryTimeout, SslHandlerProvider sslContextProvider) {
        super(Object.class);

        this.group = requireNonNull(eventLoopGroup);
        this.encoders = requireNonNull(encoders);
        this.log = LoggerFactory.getLogger("[connection-handler]" + connectionId);
        this.remoteAddress = requireNonNull(remoteAddress);
        this.onDisconnect = onDisconnect;
        this.queryTimeout = queryTimeout;
        this.sslContextProvider = sslContextProvider;
    }

    public CompletableFuture<MyConnection> connect(String username, String password, String database, Duration connectTimeout) {
        if (state != STATE_AWAITING_CONNECT_CALL)
            return failedFuture(new DatabaseException("connect() was already called"));

        state = STATE_AWAITING_SOCKET;

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        if (connectTimeout != null)
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()));

        bootstrap.handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(
                    new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 0xFFFFFF + 4, 0, 3, 1, 4, true),
                    new LongPacketMerger(),

                    ClientMessageEncoder.instance(),
                    MyConnection.this
                );
            }
        });

        CompletableFuture<MyConnection> promise = new CompletableFuture<>();

        initialHandshake = new MyHandshakeCmd(this, username, password, database, false, promise);

        ChannelFuture connectFuture = bootstrap.connect(remoteAddress);
        connectFuture.addListener(f -> {
            if (f.isSuccess()) {
                state = STATE_RUNNING_STATE_MACHINE;
                currentCommand = initialHandshake;
                handleStateMachineResult(initialHandshake.start());
            } else {
                safelyFail(initialHandshake, f.cause());
            }
        });

        return promise;
    }

    public boolean isSslSupported() {
        return sslContextProvider != null;
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

        MyCommand.Result result;

        if (currentCommand != null) {
            try {
                result = currentCommand.processPacket(packet);
                if (result == null) {
                    result = MyCommand.Result.protocolErrorAbortEverything("State machine returned no result");
                }
            } catch (Exception e) {
                result = MyCommand.Result.protocolErrorAbortEverything(e); // who knows what state it is in now..
            }
        } else {
            result = MyCommand.Result.protocolErrorAbortEverything(new IllegalStateException("A message received with no state machine active"));
        }

        handleStateMachineResult(result);
    }

    private void handleStateMachineResult(MyCommand.Result result) {
        switch (result.resultType) {
            case EXPECTING_MORE_PACKETS:
                this.channel.flush();
                break;

            case STATE_MACHINE_FINISHED:
                MyCommand next = commandQueue.pollFirst();
                if (next == null) {
                    state = STATE_IDLE;
                    currentCommand = null;
                } else {
                    state = STATE_RUNNING_STATE_MACHINE;
                    currentCommand = next;

                    MyCommand.Result initResult;
                    try {
                        initResult = currentCommand.start();
                        if (initResult == null) {
                            initResult = MyCommand.Result.protocolErrorAbortEverything("State machine returned no result");
                        }
                    } catch (Exception e) {
                        initResult = MyCommand.Result.protocolErrorAbortEverything(e);
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

            case SWITCH_TO_SSL:
                ChannelFuture channelFuture = channel.writeAndFlush(result.message);
                channelFuture.addListener(future -> {
                    System.err.println("Wrote and flushed");

                    if (!future.isSuccess()) {
                        System.err.println("Failed");
                        result.promise.completeExceptionally(future.cause());
                        return;
                    }

                    SslHandler handler = sslContextProvider.create(channel.alloc());
                    channel.pipeline().addFirst(handler);
                    handler.handshakeFuture().addListener(handshakeFuture -> {
                        System.err.println("Handshaked");
                        if (handshakeFuture.isSuccess()) {
                            System.err.println("Handshaked OK");
                            result.promise.complete(null);
                        } else {
                            System.err.println("Handshaked NOT OK");
                            result.promise.completeExceptionally(handshakeFuture.cause());
                        }
                    });
                });
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

            for (MyCommand command : commandQueue)
                safelyFail(command, new ConnectionClosedException(error));
        } finally {
            currentCommand = null;
            state = STATE_CLOSED;
            commandQueue.clear();
            channel.disconnect();
        }
    }

    private void enqueueCommand(MyCommand command) {
        commandQueue.addLast(command);

        if (state == STATE_IDLE)
            handleStateMachineResult(MyCommand.Result.stateMachineFinished());

        if (queryTimeout != null) {
            ScheduledFuture<?> future = group.schedule(this::disconnectNowDueToTimeout, queryTimeout.toMillis(), TimeUnit.MILLISECONDS);
            command.getPromise().whenComplete((result, error) -> future.cancel(false));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Channel became active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Channel became inactive");
        doCloseCleanUp(new ConnectionClosedException());
        onDisconnect.run();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof CodecException) {
            handleException(cause.getCause());
        } else {
            handleException(cause);
        }
    }

    public void handleException(Throwable cause) {
        handleStateMachineResult(MyCommand.Result.protocolErrorAbortEverything(cause));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channel) {
        this.channel = channel;
    }

    public CompletableFuture<Void> disconnect() {
        if (state == STATE_CLOSED)
            return failedFuture(new ConnectionClosedException());

        CompletableFuture<Void> promise = new CompletableFuture<>();
        enqueueCommand(new MyDisconnectCmd(this, promise));
        return promise;
    }

    private void disconnectNowDueToTimeout() {
        doCloseCleanUp(new ConnectionTimeoutException(queryTimeout));
    }

    boolean isConnected() {
        if (channel != null && channel.channel() != null) {
            return channel.channel().isActive();
        } else {
            return false;
        }
    }

    void schedule(Runnable block, Duration duration) {
        this.channel.executor().schedule(block, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public Future<Void> sendMessage(ClientMessage message) {
        switch (this.state) {
            case STATE_RUNNING_STATE_MACHINE:
                if (log.isTraceEnabled()) {
                    log.trace("Writing {}", message.toString(true));
                } else {
                    log.debug("Writing {}", message);
                }

                return this.channel.write(message);

            case STATE_IDLE:
                throw new IllegalStateException("Sending a message on an idle channel");

            case STATE_CLOSED:
                throw new IllegalStateException("Sending a message on a closed channel");

            case STATE_AWAITING_CONNECT_CALL:
                throw new IllegalStateException("Sending a message on a channel that wasn't connect()ed yet");

            case STATE_AWAITING_SOCKET:
                throw new IllegalStateException("Sending a message on a channel that isn't connected yet");
        }

        throw new IllegalStateException("Unknown state");
    }


    public void setServerInfo(HandshakeMessage serverInfo) {
        this.serverInfo = serverInfo;
    }

    public HandshakeMessage serverInfo() {
        return serverInfo;
    }

    <QR> CompletableFuture<QR> executePreparedStatement(PreparedStatementInfo psInfo, List<Object> values, MyResultSetBuilderFactory<QR> rsFactory) {
        if (values == null)
            values = Collections.emptyList();

        if (psInfo.paramDefs.size() != values.size())
            return FutureUtils.failedFuture(new SqlServerException("Column count doesn't match value count", "21S01", 1058));

        CompletableFuture<QR> promise = new CompletableFuture<>();
        enqueueCommand(new MyExecutePreparedStatementCmd<>(this, psInfo, values, encoders, rsFactory, promise));
        return promise;
    }

    CompletableFuture<Void> closePreparedStatement(PreparedStatementInfo psInfo) {
        if (psInfo.wasClosed()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> promise = new CompletableFuture<>();
        enqueueCommand(new MyClosePreparedStatementCmd(this, psInfo, promise));
        return promise.whenComplete((closeSuccess, closeError) -> {
            if (closeError == null) {
                psInfo.markAsClosed();
            }
        });
    }

    public <QR> CompletableFuture<QR> sendQuery(ByteBuf queryUtf8, MyResultSetBuilderFactory<QR> rsFactory) {
        CompletableFuture<QR> promise = new CompletableFuture<>();
        enqueueCommand(new MyTextBasedQueryCmd<>(this, queryUtf8, promise, encoders, rsFactory));
        return promise;
    }

    public CompletableFuture<MyPreparedStatement> prepareStatement(String query) {
        CompletableFuture<MyPreparedStatement> promise = new CompletableFuture<>();
        enqueueCommand(new MyPrepareStatementCmd(this, query, promise));
        return promise;
    }

    public CompletableFuture<Void> logicallyClose() {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        enqueueCommand(new MyLogicalCloseCmd(this, promise));
        return promise;
    }

    public CompletableFuture<Void> setDefaultDatabase(String database) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        enqueueCommand(new MyInitDatabaseCmd(this, database, promise));
        return promise;
    }

    public CompletableFuture<MyConnection> changeUser(String username, String password, String database) {
        CompletableFuture<MyConnection> promise = new CompletableFuture<>();
        enqueueCommand(new MyHandshakeCmd(this, username, password, database, true, promise));
        return promise;
    }

    public CompletableFuture<Void> resetConnection() {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        enqueueCommand(new MyResetConnectionCmd(this, promise));
        return promise;
    }

    public ErrorMessage decodeErrorAfterHeader(ByteBuf packet) {
        return ErrorMessage.decodeAfterHeader(packet, UTF_8, serverInfo.serverCapabilities);
    }


    private static void safelyFail(MyCommand command, Throwable cause) {
        if (command != null)
            FutureUtils.safelyFail(command.getPromise(), cause);
    }

    private static void safelyCompleteWithNull(MyCommand command) {
        if (command != null)
            FutureUtils.safelyComplete(command.getPromise(), null);
    }
}