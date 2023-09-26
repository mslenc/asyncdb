package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.DbConnection;
import com.github.mslenc.asyncdb.DbDataSource;
import com.github.mslenc.asyncdb.DbConfig;
import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.util.SslHandlerProvider;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.mslenc.asyncdb.util.FutureUtils.safelyFail;
import static java.util.Objects.requireNonNull;

public class MyDbDataSource implements DbDataSource {
    private static final int STATE_CONNECTING = 0;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_CLOSED = 2;

    private class ConnInfo {
        MyConnection conn;
        String username;
        String password;
        String database;
        int state;

        private ConnInfo() {
            state = STATE_CONNECTING;
        }

        void setConn(MyConnection conn) {
            this.conn = conn;
        }

        boolean matchesUserPassDb(String username, String password, String database) {
            return Objects.equals(username, this.username) &&
                   Objects.equals(password, this.password) &&
                   Objects.equals(database, this.database);
        }

        void setUserPassDb(String username, String password, String database) {
            this.username = username;
            this.password = password;
            this.database = database;
        }

        boolean markClosed() {
            if (state != STATE_CLOSED) {
                state = STATE_CLOSED;
                return true;
            } else {
                return false;
            }
        }

        void markConnected() {
            if (state == STATE_CONNECTING) {
                state = STATE_CONNECTED;
            }
        }
    }

    static class ConnRequest {
        final String username;
        final String password;
        final String database;
        final CompletableFuture<DbConnection> promise;

        private ConnRequest(String username, String password, String database, CompletableFuture<DbConnection> promise) {
            this.username = username;
            this.password = password;
            this.database = database;
            this.promise = promise;
        }
    }


    private static final String CONN_NAME_PREFIX = "mysql-conn-";

    private final DbConfig config;
    private long connCounter = 0;

    private int totalConnections = 0;
    private final ArrayDeque<ConnInfo> idleConnections = new ArrayDeque<>();
    private final ArrayDeque<ConnRequest> waiters = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final SocketAddress serverAddress;

    public MyDbDataSource(DbConfig config) {
        this.config = requireNonNull(config);

        serverAddress = new InetSocketAddress(config.host(), config.port());
    }

    @Override
    public CompletableFuture<DbConnection> connect() {
        return connect(config.defaultUsername(), config.defaultPassword(), config.defaultDatabase());
    }

    @Override
    public CompletableFuture<DbConnection> connect(String database) {
        return connect(config.defaultUsername(), config.defaultPassword(), database);
    }

    @Override
    public CompletableFuture<DbConnection> connect(String username, String password, String database) {
        requireNonNull(username, "username");
        requireNonNull(password, "password");
        requireNonNull(database, "database");

        CompletableFuture<DbConnection> promise = new CompletableFuture<>();

        ConnInfo existing = null;

        lock.lock();
        try {
            while (!idleConnections.isEmpty()) {
                existing = idleConnections.pollFirst();
                if (existing != null && existing.state != STATE_CLOSED)
                    break;
            }

            if (existing == null) {
                if (totalConnections < config.maxTotalConnections()) {
                    totalConnections++;
                } else {
                    waiters.add(new ConnRequest(username, password, database, promise));
                    return promise;
                }
            }
        } finally {
            lock.unlock();
        }

        if (existing != null)
            return updateUserPassDb(username, password, database, promise, existing);

        return connectAnew(username, password, database, promise);
    }

    CompletableFuture<DbConnection> connectAnew(String username, String password, String database, CompletableFuture<DbConnection> promise) {
        String connName = CONN_NAME_PREFIX + ++connCounter;

        ConnInfo connInfo = new ConnInfo();
        MyConnection conn = new MyConnection(config.eventLoopGroup(), serverAddress, connName, config.mySqlEncoders(), () -> onDisconnect(connInfo), config.queryTimeout(), makeSslContextProvider());
        connInfo.setUserPassDb(username, password, database);
        connInfo.setConn(conn);

        conn.connect(username, password, database, config.connectTimeout()).whenComplete((myConn, error) -> {
            if (error != null) {
                onDisconnect(connInfo);
                promise.completeExceptionally(error);
                return;
            }

            complete(promise, connInfo);
        });

        return promise;
    }

    private SslHandlerProvider makeSslContextProvider() {
        if (config.sslMode() == DbConfig.SslMode.DISABLE)
            return null;

        return (alloc) -> {
            SslContextBuilder builder = SslContextBuilder.forClient();

            if (config.sslMode().compareTo(DbConfig.SslMode.VERIFY_CA) >= 0) {
                if (config.rootCertFile() == null) {
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    String filename = System.getProperty("java.home") + "/lib/security/cacerts";
                    try (FileInputStream fis = new FileInputStream(filename)) {
                        keyStore.load(fis, "changeit".toCharArray());
                    }
                    tmf.init(keyStore);
                    builder.trustManager(tmf);
                }
            } else {
                builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            }

            SslContext sslContext = builder.build();

            SSLEngine engine = sslContext.newEngine(alloc, config.host(), config.port());

            if (config.sslMode() == DbConfig.SslMode.VERIFY_FULL) {
                SSLParameters params = engine.getSSLParameters();
                params.setEndpointIdentificationAlgorithm("HTTPS");
                engine.setSSLParameters(params);
            }

            return new SslHandler(engine);
        };
    }

    CompletableFuture<DbConnection> updateUserPassDb(String username, String password, String database, CompletableFuture<DbConnection> promise, ConnInfo connInfo) {
        CompletableFuture<?> future;

        // So we used to do connInfo.conn.resetConnection() when the username/pass/db all matched,
        // but, it failed to work - the client charset would reset to latin1 (on default mysql5
        // configuration) and non-ascii chars would be totally broken.
        // Whoever designed this to work like it does is a big moron.
        future = connInfo.conn.changeUser(username, password, database);

        future.whenComplete((result, error) -> {
            if (error != null) {
                safelyFail(promise, error);
                connInfo.conn.disconnect();
                return;
            }

            connInfo.setUserPassDb(username, password, database);
            complete(promise, connInfo);
        });

        return promise;
    }

    void complete(CompletableFuture<DbConnection> promise, ConnInfo connInfo) {
        connInfo.markConnected();
        MyDbConnection wrapper = new MyDbConnection(connInfo.conn, config, () -> returnConnection(connInfo));
        wrapper.runInitStatements(config.initStatements(), promise);
    }

    void returnConnection(ConnInfo connInfo) {
        ConnRequest request = null;
        lock.lock();
        try {
            if (totalConnections <= config.maxTotalConnections() && !waiters.isEmpty()) {
                request = waiters.pollFirst();
            } else {
                if (idleConnections.size() < config.maxIdleConnections()) {
                    idleConnections.addLast(connInfo);
                } else {
                    connInfo.conn.disconnect(); // and stuff will be updated in onDisconnect()
                }
            }
        } finally {
            lock.unlock();
        }

        if (request != null) {
            updateUserPassDb(request.username, request.password, request.database, request.promise, connInfo);
        }
    }

    void onDisconnect(ConnInfo connInfo) {
        if (!connInfo.markClosed())
            return;

        ConnRequest request = null;
        lock.lock();
        try {
            if (totalConnections <= config.maxTotalConnections() && !waiters.isEmpty()) {
                request = waiters.pollFirst();
            } else {
                totalConnections--;
            }
        } finally {
            lock.unlock();
        }

        if (request != null) {
            connectAnew(request.username, request.password, request.database, request.promise);
        }
    }
}
