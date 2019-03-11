package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JdbcNonAsyncDbConnection implements DbConnection {
    private final JdbcSyncConnection conn;

    public JdbcNonAsyncDbConnection(JdbcSyncConnection conn) {
        this.conn = conn;
    }

    @Override
    public DbConfig getConfig() {
        return conn.getConfig();
    }

    @Override
    public CompletableFuture<DbExecResult> execute(String sql) {
        CompletableFuture<DbExecResult> future = new CompletableFuture<>();
        try {
            future.complete(conn.execute(sql));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbExecResult> execute(String sql, List<Object> values) {
        CompletableFuture<DbExecResult> future = new CompletableFuture<>();
        try {
            future.complete(conn.execute(sql, values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbExecResult> execute(String sql, Object... values) {
        CompletableFuture<DbExecResult> future = new CompletableFuture<>();
        try {
            future.complete(conn.execute(sql, values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbResultSet> executeQuery(String sql) {
        CompletableFuture<DbResultSet> future = new CompletableFuture<>();
        try {
            future.complete(conn.executeQuery(sql));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbResultSet> executeQuery(String sql, List<Object> values) {
        CompletableFuture<DbResultSet> future = new CompletableFuture<>();
        try {
            future.complete(conn.executeQuery(sql, values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbResultSet> executeQuery(String sql, Object... values) {
        CompletableFuture<DbResultSet> future = new CompletableFuture<>();
        try {
            future.complete(conn.executeQuery(sql, values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbUpdateResult> executeUpdate(String sql) {
        CompletableFuture<DbUpdateResult> future = new CompletableFuture<>();
        try {
            future.complete(conn.executeUpdate(sql));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbUpdateResult> executeUpdate(String sql, List<Object> values) {
        CompletableFuture<DbUpdateResult> future = new CompletableFuture<>();
        try {
            future.complete(conn.executeUpdate(sql, values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbUpdateResult> executeUpdate(String sql, Object... values) {
        CompletableFuture<DbUpdateResult> future = new CompletableFuture<>();
        try {
            future.complete(conn.executeUpdate(sql, values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public void streamQuery(String sql, DbQueryResultObserver streamHandler) {
        conn.streamQuery(sql, streamHandler);
    }

    @Override
    public void streamQuery(String sql, DbQueryResultObserver streamHandler, List<Object> values) {
        conn.streamQuery(sql, streamHandler, values);
    }

    @Override
    public void streamQuery(String sql, DbQueryResultObserver streamHandler, Object... values) {
        conn.streamQuery(sql, streamHandler, values);
    }

    @Override
    public CompletableFuture<DbPreparedStatement> prepareStatement(String sql) {
        CompletableFuture<DbPreparedStatement> future = new CompletableFuture<>();
        try {
            JdbcSyncPreparedStatement ps = conn.prepareStatement(sql);
            future.complete(new JdbcNonAsyncDbPreparedStatement(ps));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> startTransaction() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.startTransaction();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> startTransaction(DbTxIsolation isolation) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.startTransaction(isolation);
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> startTransaction(DbTxMode mode) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.startTransaction(mode);
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> startTransaction(DbTxIsolation isolation, DbTxMode mode) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.startTransaction(isolation, mode);
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> commit() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.commit();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> commitAndChain() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.commitAndChain();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> rollback() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.rollback();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> rollbackAndChain() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.rollbackAndChain();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> close() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            conn.close();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }
}
