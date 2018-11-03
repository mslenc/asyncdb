package com.github.mslenc.asyncdb;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.util.MiscUtils.extractResultSet;
import static com.github.mslenc.asyncdb.util.MiscUtils.extractUpdateResult;
import static java.util.Arrays.asList;

public interface DbPreparedStatement {
    DbColumns getColumns();
    DbColumns getParameters();

    CompletableFuture<DbExecResult> execute(List<Object> values);

    default CompletableFuture<DbExecResult> execute(Object... values) {
        return execute(asList(values));
    }

    default CompletableFuture<DbResultSet> executeQuery(List<Object> values) {
        return extractResultSet(execute(values));
    }

    default CompletableFuture<DbResultSet> executeQuery(Object... values) {
        return extractResultSet(execute(asList(values)));
    }

    default CompletableFuture<DbUpdateResult> executeUpdate(List<Object> values) {
        return extractUpdateResult(execute(values));
    }

    default CompletableFuture<DbUpdateResult> executeUpdate(Object... values) {
        return extractUpdateResult(execute(asList(values)));
    }

    void streamQuery(DbQueryResultObserver streamHandler, List<Object> values);

    default void streamQuery(DbQueryResultObserver streamHandler, Object... values) {
        streamQuery(streamHandler, asList(values));
    }

    CompletableFuture<Void> close();
}
