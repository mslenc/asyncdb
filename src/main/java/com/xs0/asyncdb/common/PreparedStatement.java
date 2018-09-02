package com.xs0.asyncdb.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PreparedStatement {
    int getNumberOfParameters();
    int getNumberOfColumns();

    CompletableFuture<QueryResult> execute(List<Object> values);
    CompletableFuture<Void> close();
}
