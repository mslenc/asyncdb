package com.github.mslenc.asyncdb;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.util.MiscUtils.extractResultSet;
import static com.github.mslenc.asyncdb.util.MiscUtils.extractUpdateResult;

public interface DbConnection {
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

    CompletableFuture<Void> close();
}