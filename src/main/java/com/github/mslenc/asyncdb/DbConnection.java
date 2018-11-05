package com.github.mslenc.asyncdb;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.util.MiscUtils.extractResultSet;
import static com.github.mslenc.asyncdb.util.MiscUtils.extractUpdateResult;
import static com.github.mslenc.asyncdb.util.MiscUtils.extractVoid;

public interface DbConnection {
    DbConfig getConfig();

    CompletableFuture<DbExecResult> execute(String sql);
    CompletableFuture<DbExecResult> execute(String sql, List<Object> values);

    default CompletableFuture<DbExecResult> execute(String sql, Object... values) {
        return execute(sql, Arrays.asList(values));
    }

    default CompletableFuture<DbResultSet> executeQuery(String sql) {
        return extractResultSet(execute(sql));
    }

    default CompletableFuture<DbResultSet> executeQuery(String sql, List<Object> values) {
        return extractResultSet(execute(sql, values));
    }

    default CompletableFuture<DbResultSet> executeQuery(String sql, Object... values) {
        return extractResultSet(execute(sql, values));
    }

    default CompletableFuture<DbUpdateResult> executeUpdate(String sql) {
        return extractUpdateResult(execute(sql));
    }

    default CompletableFuture<DbUpdateResult> executeUpdate(String sql, List<Object> values) {
        return extractUpdateResult(execute(sql, values));
    }

    default CompletableFuture<DbUpdateResult> executeUpdate(String sql, Object... values) {
        return extractUpdateResult(execute(sql, values));
    }

    void streamQuery(String sql, DbQueryResultObserver streamHandler);
    void streamQuery(String sql, DbQueryResultObserver streamHandler, List<Object> values);

    default void streamQuery(String sql, DbQueryResultObserver streamHandler, Object... values) {
        streamQuery(sql, streamHandler, Arrays.asList(values));
    }

    CompletableFuture<DbPreparedStatement> prepareStatement(String sql);


    default CompletableFuture<Void> startTransaction() {
        DbConfig config = getConfig();
        return startTransaction(config.defaultTxIsolation(), config.defaultTxMode());
    }

    default CompletableFuture<Void> startTransaction(DbTxIsolation isolation) {
        return startTransaction(isolation, getConfig().defaultTxMode());
    }

    default CompletableFuture<Void> startTransaction(DbTxMode mode) {
        return startTransaction(getConfig().defaultTxIsolation(), mode);
    }

    default CompletableFuture<Void> startTransaction(DbTxIsolation isolation, DbTxMode mode) {
        DbConfig config = getConfig();

        String beginTx;
        if (mode == DbTxMode.READ_ONLY) {
            beginTx = "START TRANSACTION READ ONLY";
        } else {
            beginTx = "START TRANSACTION READ WRITE";
        }

        if (isolation == config.defaultTxIsolation()) {
            return extractVoid(execute(beginTx));
        } else {
            CompletableFuture<Void> promise = new CompletableFuture<>();

            execute("SET TRANSACTION ISOLATION LEVEL " + isolation).whenComplete((result, error) -> {
                if (error != null) {
                    promise.completeExceptionally(error);
                } else {
                    execute(beginTx).whenComplete((result2, error2) -> {
                        if (error2 != null) {
                            promise.completeExceptionally(error2);
                        } else {
                            promise.complete(null);
                        }
                    });
                }
            });

            return promise;
        }
    }

    default CompletableFuture<Void> commit() {
        return extractVoid(execute("COMMIT"));
    }

    default CompletableFuture<Void> commitAndChain() {
        return extractVoid(execute("COMMIT AND CHAIN"));
    }

    default CompletableFuture<Void> rollback() {
        return extractVoid(execute("ROLLBACK"));
    }

    default CompletableFuture<Void> rollbackAndChain() {
        return extractVoid(execute("ROLLBACK AND CHAIN"));
    }

    CompletableFuture<Void> close();
}