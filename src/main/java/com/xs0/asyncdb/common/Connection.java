package com.xs0.asyncdb.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Connection {
    CompletableFuture<Connection> disconnect();

    CompletableFuture<Connection> connect();

    boolean isConnected();

    CompletableFuture<QueryResult> sendQuery(String query);

    CompletableFuture<QueryResult> sendPreparedStatement(String query, List<Object> values);

    ExecutionContext executionContext();

    default <T> CompletableFuture<T> inTransaction(TransactionExecutor<T> transactionExecutor) {
        CompletableFuture<T> future = new CompletableFuture<>();

        sendQuery("BEGIN").whenCompleteAsync((beginResult, beginError) -> {
            if (beginError != null) {
                future.completeExceptionally(beginError);
                return;
            }

            CompletableFuture<T> innerFuture;
            try {
                innerFuture = transactionExecutor.executeTransaction(this);
            } catch (Exception e) {
                sendQuery("ROLLBACK").thenRun(() -> {
                    future.completeExceptionally(e);
                });
                return;
            }

            innerFuture.whenComplete((txResult, txError) -> {
                if (txError != null) {
                    sendQuery("ROLLBACK").thenRun(() -> {
                        future.completeExceptionally(txError);
                    });
                } else {
                    sendQuery("COMMIT").whenComplete((commitResult, commitError) -> {
                        if (commitError != null) {
                            future.completeExceptionally(commitError);
                        } else {
                            future.complete(txResult);
                        }
                    });
                }
            });
        }, executionContext());

        return future;
    }
}
