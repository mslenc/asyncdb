package com.xs0.asyncdb.mysql;

import com.xs0.asyncdb.common.*;
import com.xs0.asyncdb.common.pool.TimeoutScheduler;
import com.xs0.asyncdb.common.util.NettyUtils;
import com.xs0.asyncdb.common.util.Version;
import com.xs0.asyncdb.mysql.codec.MySQLConnectionHandler;
import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class MySQLConnection extends TimeoutScheduler implements Connection {
    private static AtomicLong counter = new AtomicLong();
    private static Version microsecondsVersion = new Version(5, 6, 0);
    private static Logger log = LoggerFactory.getLogger(MySQLConnection.class);

    private final EventLoopGroup group;

    private final long connectionCount;
    private final String connectionId;

    private final CompletableFuture<Connection> connectionPromise;

    private final MySQLConnectionHandler connectionHandler;
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

        this.group = group;

        this.connectionCount = counter.incrementAndGet();
        this.connectionId = "[mysql-connection-" + connectionCount + "]";

        this.connectionHandler = new MySQLConnectionHandler(
            configuration,
            group,
            connectionId
        );

        this.connectionPromise = new CompletableFuture<>();
    }

    public Version version() {
        return serverVersion;
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
            } else {
                connectionPromise.complete(this);
            }
        });

        return connectionPromise;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        return this.connectionHandler.disconnect();
    }

    @Override
    public CompletableFuture<QueryResult> sendQuery(String query) {
        return connectionHandler.sendQuery(query);
    }

    @Override
    public CompletableFuture<QueryResult> sendQuery(String query, List<Object> values) {
        return connectionHandler.sendQuery(query, values);
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
        return this.connectionHandler.prepareStatement(query);
    }
}
