package com.github.mslenc.asyncdb.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Connection {
    CompletableFuture<Connection> connect();
    CompletableFuture<Void> disconnect();
    boolean isConnected();


    CompletableFuture<QueryResult> sendQuery(String query);

    CompletableFuture<PreparedStatement> prepareStatement(String query);

    CompletableFuture<QueryResult> sendQuery(String query, List<Object> values);
}
