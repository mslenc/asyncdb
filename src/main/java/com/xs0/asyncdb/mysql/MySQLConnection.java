package com.xs0.asyncdb.mysql;

import com.xs0.asyncdb.common.*;
import com.xs0.asyncdb.common.exceptions.ConnectionStillRunningQueryException;
import com.xs0.asyncdb.common.exceptions.DatabaseException;
import com.xs0.asyncdb.common.exceptions.InsufficientParametersException;
import com.xs0.asyncdb.common.pool.TimeoutScheduler;
import com.xs0.asyncdb.common.util.ExecutorServiceUtils;
import com.xs0.asyncdb.common.util.NettyUtils;
import com.xs0.asyncdb.common.util.Version;
import com.xs0.asyncdb.mysql.codec.MySQLConnectionHandler;
import com.xs0.asyncdb.mysql.codec.MySQLHandlerDelegate;
import com.xs0.asyncdb.mysql.ex.MySQLException;
import com.xs0.asyncdb.mysql.message.client.AuthenticationSwitchResponse;
import com.xs0.asyncdb.mysql.message.client.HandshakeResponseMessage;
import com.xs0.asyncdb.mysql.message.client.QueryMessage;
import com.xs0.asyncdb.mysql.message.client.QuitMessage;
import com.xs0.asyncdb.mysql.message.server.*;
import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MySQLConnection extends TimeoutScheduler implements MySQLHandlerDelegate, Connection {
    private static AtomicLong counter = new AtomicLong();
    private static Version microsecondsVersion = new Version(5, 6, 0);
    private static Logger log = LoggerFactory.getLogger(MySQLConnection.class);

    private final Configuration configuration;
    private final CharsetMapper charsetMapper;
    private final EventLoopGroup group;
    private final ExecutionContext executionContext;

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
                           EventLoopGroup group,
                           ExecutionContext executionContext
                           ) {
        if (configuration == null)
            throw new IllegalArgumentException("Missing configuration");

        if (charsetMapper == null)
            charsetMapper = CharsetMapper.instance();
        if (group == null)
            group = NettyUtils.defaultEventLoopGroup;
        if (executionContext == null)
            executionContext = ExecutorServiceUtils.wrap(group);

        charsetMapper.toInt(configuration.charset); // (verify support for charset)

        this.configuration = configuration;
        this.charsetMapper = charsetMapper;
        this.group = group;
        this.executionContext = executionContext;

        this.connectionCount = counter.incrementAndGet();
        this.connectionId = "[mysql-connection-" + connectionCount + "]";

        this.connectionHandler = new MySQLConnectionHandler(
            configuration,
            charsetMapper,
            this,
            group,
            executionContext,
            connectionId
        );

        this.connectionPromise = new CompletableFuture<>();
        this.disconnectionPromise = new CompletableFuture<>();
    }

    @Override
    public ExecutionContext executionContext() {
        return executionContext;
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

    public CompletableFuture<Connection> close() {
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
    }

    @Override
    public void connected(ChannelHandlerContext ctx) {
        if (log.isDebugEnabled())
            log.debug("Connected to {}", ctx.channel().remoteAddress());
        this.connected = true;
    }

    @Override
    public void exceptionCaught(Throwable exception) {
        log.error("Transpoort failure", exception);
        setException(exception);
    }

    @Override
    public void onError(ErrorMessage message) {
        log.error("Received an error message -> {}", message);
        Exception exception = new MySQLException(message);
        exception.fillInStackTrace();
        setException(exception);
    }

    private void setException(Throwable t) {
        this._lastException = t;
        this.connectionPromise.completeExceptionally(t);
        this.failQueryPromise(t);
    }

    @Override
    public void onOk(OkMessage okMessage) {
        if (!this.connectionPromise.isDone()) {
            log.debug("Connected to database");
            this.connectionPromise.complete(this);
        } else
        if (this.isQuerying()) {
            this.succeedQueryPromise(new MySQLQueryResult(
                okMessage.affectedRows,
                okMessage.message,
                okMessage.lastInsertId,
                okMessage.statusFlags,
                okMessage.warnings,
                null // (resultSet)
            ));
        } else {
            log.warn("Received OK when not querying or connecting, most strange.");
        }
    }

    @Override
    public void onEOF(EOFMessage message) {
        if (this.isQuerying()) {
            this.succeedQueryPromise(new MySQLQueryResult(
                0,
                    null,
                    -1,
                    message.flags,
                    message.warningCount,
                    null
            ));
        }
    }

    @Override
    public void onHandshake(HandshakeMessage message) {
        this.serverVersion = Version.parse(message.serverVersion);

        this.connectionHandler.write(new HandshakeResponseMessage(
            configuration.username,
            configuration.charset,
            message.seed,
            message.authenticationMethod,
            configuration.password,
            configuration.database
        ));
    }

    @Override
    public void switchAuthentication(AuthenticationSwitchRequest message) {
        this.connectionHandler.write(new AuthenticationSwitchResponse(configuration.password, message));
    }

    @Override
    public CompletableFuture<QueryResult> sendQuery(String query) {
        this.validateIsReadyForQuery();
        CompletableFuture<QueryResult> promise = new CompletableFuture<>();
        this.setQueryPromise(promise);
        this.connectionHandler.write(new QueryMessage(query));
        addTimeout(promise, configuration.queryTimeout);
        return promise;
    }

    private void failQueryPromise(Throwable t) {
        CompletableFuture<QueryResult> promiseMaybe = this.clearQueryPromise();
        if (promiseMaybe != null)
            promiseMaybe.completeExceptionally(t);
    }

    private void succeedQueryPromise(QueryResult queryResult) {
        CompletableFuture<QueryResult> promiseMaybe = this.clearQueryPromise();
        if (promiseMaybe != null)
            promiseMaybe.complete(queryResult);
    }

    boolean isQuerying() {
        return this.queryPromiseReference.get() != null;
    }

    @Override
    public void onResultSet(ResultSet resultSet, EOFMessage message) {
        this.succeedQueryPromise(new MySQLQueryResult(
            resultSet.size(),
            null,
            -1,
            message.flags,
            message.warningCount,
            resultSet
        ));
    }

    @Override
    public CompletableFuture<Connection> disconnect() {
        return close();
    }

    @Override
    public boolean isConnected() {
        return this.connectionHandler.isConnected();
    }

    @Override
    public CompletableFuture<QueryResult> sendPreparedStatement(String query, List<Object> values) {
        this.validateIsReadyForQuery();
        int totalParameters = countParameterMarks(query);
        if (totalParameters != values.size()) {
            throw new InsufficientParametersException(totalParameters, values.size());
        }
        CompletableFuture<QueryResult> promise = new CompletableFuture<>();
        this.setQueryPromise(promise);
        this.connectionHandler.sendPreparedStatement(query, values);
        addTimeout(promise, configuration.queryTimeout);
        return promise;
    }

    private void validateIsReadyForQuery() {
        if (isQuerying()) {
            throw new ConnectionStillRunningQueryException(this.connectionCount, false);
        }
    }

    void setQueryPromise(CompletableFuture<QueryResult> promise) {
        if (!queryPromiseReference.compareAndSet(null, promise)) {
            throw new ConnectionStillRunningQueryException(this.connectionCount, true);
        }
    }

    CompletableFuture<QueryResult> clearQueryPromise() {
        return queryPromiseReference.getAndSet(null);
    }

    @Override
    protected void onTimeout() {
        disconnect();
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
