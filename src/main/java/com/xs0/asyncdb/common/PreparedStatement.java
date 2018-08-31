package com.xs0.asyncdb.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PreparedStatement {
    CompletableFuture<QueryResult> execute(List<Object> values);
    CompletableFuture<Void> close();
}
