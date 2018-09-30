package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.common.Configuration;
import com.github.mslenc.asyncdb.common.Connection;
import com.github.mslenc.asyncdb.common.PreparedStatement;
import com.github.mslenc.asyncdb.common.QueryResult;
import com.github.mslenc.asyncdb.common.pool.TimeoutScheduler;
import com.github.mslenc.asyncdb.common.sql.SqlLiteralEncoders;
import com.github.mslenc.asyncdb.common.sql.SqlQueryPlaceholders;
import com.github.mslenc.asyncdb.common.util.NettyUtils;
import com.github.mslenc.asyncdb.common.util.Version;
import com.github.mslenc.asyncdb.mysql.codec.MySQLConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.mslenc.asyncdb.common.util.FutureUtils.failedFuture;

public class MySQLConnection extends TimeoutScheduler implements Connection {
    private static AtomicLong counter = new AtomicLong();
    private static Version microsecondsVersion = new Version(5, 6, 0);
    private static Logger log = LoggerFactory.getLogger(MySQLConnection.class);
    public static final String DEFAULT_INIT_SQL = "set session time_zone='+00:00', sql_mode='STRICT_ALL_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO,ANSI_QUOTES', autocommit=1";

    private final EventLoopGroup group;

    private final long connectionCount;
    private final String connectionId;

    private final CompletableFuture<Connection> connectionPromise;

    private final MySQLConnectionHandler connectionHandler;
    private Version serverVersion = null;

    public MySQLConnection(Configuration configuration,
                           EventLoopGroup group
                           ) {
        if (configuration == null)
            throw new IllegalArgumentException("Missing configuration");

        if (group == null)
            group = NettyUtils.defaultEventLoopGroup;

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

    private CompletableFuture<Connection> doConnect() {
        this.connectionHandler.connect().whenComplete((ignored, connectError) -> {
            if (connectError != null) {
                connectionPromise.completeExceptionally(connectError);
            } else {
                connectionPromise.complete(this);
            }
        });

        return connectionPromise;
    }

    public CompletableFuture<Connection> connect() {
        return connectAndInit(DEFAULT_INIT_SQL);
    }

    public CompletableFuture<Connection> connectAndInit(String... sqls) {
        CompletableFuture<Connection> promise = new CompletableFuture<>();

        doConnect().whenComplete((conn, connError) -> {
            if (connError != null) {
                promise.completeExceptionally(connError);
                return;
            }

            continueSendingInitStatements(conn, promise, sqls, 0);
        });

        return promise;
    }

    void continueSendingInitStatements(Connection conn, CompletableFuture<Connection> promise, String[] sqls, int sqlIndex) {
        if (sqlIndex >= sqls.length) {
            promise.complete(conn);
            return;
        }

        conn.sendQuery(sqls[sqlIndex]).whenComplete((result, error) -> {
            if (error != null) {
                promise.completeExceptionally(error);
                return;
            }

            continueSendingInitStatements(conn, promise, sqls, sqlIndex + 1);
        });
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        return connectionHandler.disconnect();
    }

    @Override
    public CompletableFuture<QueryResult> sendQuery(String query) {
        ByteBuf queryUtf8 = Unpooled.copiedBuffer(query, StandardCharsets.UTF_8);
        return connectionHandler.sendQuery(queryUtf8);
    }

    @Override
    public CompletableFuture<QueryResult> sendQuery(String query, List<Object> values) {
        ByteBuf queryUtf8WithValues = Unpooled.buffer(query.length() + values.size() * 20 + 10);

        try {
            SqlQueryPlaceholders.insertValuesForPlaceholders(query, values, SqlLiteralEncoders.DEFAULT, connectionHandler.codecSettings, queryUtf8WithValues);
        } catch (Exception e) {
            return failedFuture(e);
        }

        return connectionHandler.sendQuery(queryUtf8WithValues);
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
