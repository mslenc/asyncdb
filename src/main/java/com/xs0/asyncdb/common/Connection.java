package com.xs0.asyncdb.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Connection {
    CompletableFuture<Connection> disconnect();

    CompletableFuture<Connection> connect();

    boolean isConnected();

    CompletableFuture<QueryResult> sendQuery(String query);

    CompletableFuture<QueryResult> sendPreparedStatement(String query, List<Object> values);

    default <T> CompletableFuture<T> inTransaction(TransactionExecutor<T> transactionExecutor) {
        CompletableFuture<T> future = new CompletableFuture<>();

        sendQuery("BEGIN").handle((result, error) -> {
            if (error != null) {
                future.completeExceptionally(error);
                return null;
            }

            CompletableFuture<T> innerFuture;
            try {
                innerFuture = transactionExecutor.executeTransaction(this);
            } catch (Exception e) {
                sendQuery("ROLLBACK").handle((ignoredResult, ignoredError) -> {
                    future.completeExceptionally(e);
                    return null;
                });
                return null;
            }

            innerFuture.handle((finalResult, finalError) -> {
                if (finalError != null) {
                    sendQuery("ROLLBACK").handle((ignoredResult, ignoredError) -> {
                        future.completeExceptionally(finalError);
                        return null;
                    });
                } else {
                    sendQuery("COMMIT").handle((commitResult, commitError) -> {
                        if (commitError != null) {
                            future.completeExceptionally(commitError);
                        } else {
                            future.complete(finalResult);
                        }

                        return null;
                    });
                }

                return null;
            });

            return null;
        });

        return future;
    }
}
