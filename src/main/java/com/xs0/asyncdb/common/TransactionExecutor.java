package com.xs0.asyncdb.common;

import java.util.concurrent.CompletableFuture;

public interface TransactionExecutor<T> {
    CompletableFuture<T> executeTransaction(Connection connection);
}
