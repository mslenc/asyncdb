package com.xs0.asyncdb.mysql;

import com.xs0.asyncdb.common.*;
import com.xs0.asyncdb.common.pool.TimeoutScheduler;
import com.xs0.asyncdb.common.util.NettyUtils;
import com.xs0.asyncdb.common.util.Version;
import com.xs0.asyncdb.mysql.codec.MySQLConnectionHandler;
import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MySQLConnection extends TimeoutScheduler implements Connection {
    private static AtomicLong counter = new AtomicLong();
    private static Version microsecondsVersion = new Version(5, 6, 0);
    private static Logger log = LoggerFactory.getLogger(MySQLConnection.class);

    private final Configuration configuration;
    private final CharsetMapper charsetMapper;
    private final EventLoopGroup group;

    private final long connectionCount;
    private final String connectionId;

    private final CompletableFuture<Connection> connectionPromise;
    private final CompletableFuture<Connection> disconnectionPromise;

    private final AtomicReference<CompletableFuture<QueryResult>> queryPromiseReference = new AtomicReference<>(null);
    private final MySQLConnectionHandler connectionHandler;
    private boolean connected = false;
    private Throwable _lastException = null;
    private Version serverVersion = null;

    public MySQLConnection(Configuration configuration,
                           CharsetMapper charsetMapper,
                           EventLoopGroup group
                           ) {
        if (configuration == null)
            throw new IllegalArgumentException("Missing configuration");

        if (charsetMapper == null)
            charsetMapper = CharsetMapper.instance();
        if (group == null)
            group = NettyUtils.defaultEventLoopGroup;

        charsetMapper.toInt(configuration.charset); // (verify support for charset)

        this.configuration = configuration;
        this.charsetMapper = charsetMapper;
        this.group = group;

        this.connectionCount = counter.incrementAndGet();
        this.connectionId = "[mysql-connection-" + connectionCount + "]";

        this.connectionHandler = new MySQLConnectionHandler(
            configuration,
            this,
            group,
            connectionId
        );

        this.connectionPromise = new CompletableFuture<>();
        this.disconnectionPromise = new CompletableFuture<>();
    }

    public Version version() {
        return serverVersion;
    }

    public Throwable lastException() {
        return _lastException;
    }

    public long count() {
        return connectionCount;
    }

    @Override
    protected EventLoopGroup eventLoopGroup() {
        return group;
    }

    public CompletableFuture<Connection> connect() {
        this.connectionHandler.connect().whenComplete((ignored, connectError) -> {
            if (connectError != null) {
                connectionPromise.completeExceptionally(connectError);
            }
            // (we complete() successfully later, after handshake is completed..)
        });

        return connectionPromise;
    }

    @Override
    public CompletableFuture<Connection> disconnect() {
        throw new UnsupportedOperationException("disconnect");
        // TODO
    }

    public CompletableFuture<Void> close() {
        /*
        if (this.isConnected()) {
            if (!this.disconnectionPromise.isDone()) {
                DatabaseException exception = new DatabaseException("Connection is being closed");
                exception.fillInStackTrace();
                this.failQueryPromise(exception);
                this.connectionHandler.clearQueryState();
                this.connectionHandler.write(QuitMessage.instance()).addListener(quitResult -> {
                    if (quitResult.isSuccess()) {
                        this.connectionHandler.disconnect().addListener(disconnectResult -> {
                            if (disconnectResult.isSuccess()) {
                                disconnectionPromise.complete(this);
                            } else {
                                disconnectionPromise.completeExceptionally(disconnectResult.cause());
                            }
                        });
                    } else {
                        disconnectionPromise.completeExceptionally(quitResult.cause());
                    }
                });
            }
        }

        return disconnectionPromise;
        */
        // TODO
        return CompletableFuture.completedFuture(null);
    }

    public void connected(ChannelHandlerContext ctx) {
        if (log.isDebugEnabled())
            log.debug("Connected to {}", ctx.channel().remoteAddress());
        this.connected = true;
    }

    @Override
    public CompletableFuture<QueryResult> sendQuery(String query) {
        return connectionHandler.sendQuery(query);
    }

    @Override
    public boolean isConnected() {
        return this.connectionHandler.isConnected();
    }

    @Override
    protected void onTimeout() {
        disconnect();
    }

    @Override
    public CompletableFuture<PreparedStatement> prepareStatement(String query) {
        throw new UnsupportedOperationException();
    }

    static int countParameterMarks(String query) {
        int mode = 0; // 1 = inside ', 2 = inside ", 3 = inside `, 16 = after \

        int parameterCount = 0;

        for (int i = 0, len = query.length(); i < len; i++) {
            if ((mode & 16) == 16) { // after backslash
                mode ^= 16;
                continue;
            }

            switch (query.charAt(i)) {
                case '\\':
                    if (mode == 0) { // outside a string.. who knows what the \ is supposed to be..
                        // ignore
                    } else {
                        mode |= 16; // enter escape mode, next char handled above..
                    }
                    break;

                case '\'':
                    if (mode == 0) {
                        mode = 1;
                    } else
                    if (mode == 1) {
                        mode = 0;
                    }
                    break;

                case '"':
                    if (mode == 0) {
                        mode = 2;
                    } else
                    if (mode == 2) {
                        mode = 0;
                    }
                    break;

                case '`':
                    if (mode == 0) {
                        mode = 3;
                    } else
                    if (mode == 3) {
                        mode = 0;
                    }
                    break;

                case '?':
                    if (mode == 0) {
                        parameterCount++;
                    }
                    break;
            }
        }

        return parameterCount;
    }
}
