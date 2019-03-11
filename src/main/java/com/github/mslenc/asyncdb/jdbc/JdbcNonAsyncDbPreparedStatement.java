package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JdbcNonAsyncDbPreparedStatement implements DbPreparedStatement  {
    private final JdbcSyncPreparedStatement ps;

    public JdbcNonAsyncDbPreparedStatement(JdbcSyncPreparedStatement ps) {
        this.ps = ps;
    }

    @Override
    public DbColumns getColumns() {
        return ps.getColumns();
    }

    @Override
    public DbColumns getParameters() {
        return ps.getParameters();
    }

    @Override
    public CompletableFuture<DbExecResult> execute(List<Object> values) {
        CompletableFuture<DbExecResult> future = new CompletableFuture<>();
        try {
            future.complete(ps.execute(values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbExecResult> execute(Object... values) {
        CompletableFuture<DbExecResult> future = new CompletableFuture<>();
        try {
            future.complete(ps.execute(values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbResultSet> executeQuery(List<Object> values) {
        CompletableFuture<DbResultSet> future = new CompletableFuture<>();
        try {
            future.complete(ps.executeQuery(values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbResultSet> executeQuery(Object... values) {
        CompletableFuture<DbResultSet> future = new CompletableFuture<>();
        try {
            future.complete(ps.executeQuery(values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbUpdateResult> executeUpdate(List<Object> values) {
        CompletableFuture<DbUpdateResult> future = new CompletableFuture<>();
        try {
            future.complete(ps.executeUpdate(values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public CompletableFuture<DbUpdateResult> executeUpdate(Object... values) {
        CompletableFuture<DbUpdateResult> future = new CompletableFuture<>();
        try {
            future.complete(ps.executeUpdate(values));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public void streamQuery(DbQueryResultObserver streamHandler, List<Object> values) {
        ps.streamQuery(streamHandler, values);
    }

    @Override
    public void streamQuery(DbQueryResultObserver streamHandler, Object... values) {
        ps.streamQuery(streamHandler, values);
    }

    @Override
    public CompletableFuture<Void> close() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            ps.close();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }
}
