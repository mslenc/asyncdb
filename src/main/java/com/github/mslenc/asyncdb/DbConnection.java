package com.github.mslenc.asyncdb;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DbConnection {
    CompletableFuture<DbQueryResult> sendQuery(String sql);
    CompletableFuture<DbQueryResult> sendQuery(String sql, List<Object> values);

    default CompletableFuture<DbQueryResult> sendQuery(String sql, Object... values) {
        return sendQuery(sql, Arrays.asList(values));
    }

    void streamQuery(String sql, DbResultObserver streamHandler);
    void streamQuery(String sql, DbResultObserver streamHandler, List<Object> values);

    default void streamQuery(String sql, DbResultObserver streamHandler, Object... values) {
        streamQuery(sql, streamHandler, Arrays.asList(values));
    }

    CompletableFuture<DbPreparedStatement> prepareStatement(String sql);

    CompletableFuture<Void> close();
}