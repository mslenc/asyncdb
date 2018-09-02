package com.xs0.asyncdb.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Connection {
    CompletableFuture<Connection> connect();
    CompletableFuture<Void> disconnect();
    boolean isConnected();


    CompletableFuture<QueryResult> sendQuery(String query);

    CompletableFuture<PreparedStatement> prepareStatement(String query);

    default CompletableFuture<QueryResult> sendQuery(String query, List<Object> values) {
        CompletableFuture<QueryResult> promise = new CompletableFuture<>();

        prepareStatement(query).whenComplete((preparedStatement, prepareError) -> {
            if (prepareError != null) {
                promise.completeExceptionally(prepareError);
                return;
            }

            preparedStatement.execute(values).whenComplete((executeResult, executeError) -> {
                preparedStatement.close().thenRun(() -> {
                    if (executeError != null) {
                        promise.completeExceptionally(executeError);
                    } else {
                        promise.complete(executeResult);
                    }
                });
            });
        });

        return promise;
    }
}
