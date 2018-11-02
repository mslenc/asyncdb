package com.github.mslenc.asyncdb;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DbPreparedStatement {
    DbColumns getColumns();
    DbColumns getParameters();

    CompletableFuture<DbQueryResult> execute(List<Object> values);

    default CompletableFuture<DbQueryResult> execute(Object... values) {
        return execute(Arrays.asList(values));
    }

    void stream(DbResultObserver streamHandler, List<Object> values);

    default void stream(DbResultObserver streamHandler, Object... values) {
        stream(streamHandler, Arrays.asList(values));
    }

    CompletableFuture<Void> close();
}
