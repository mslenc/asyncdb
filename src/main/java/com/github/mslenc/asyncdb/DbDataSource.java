package com.github.mslenc.asyncdb;

import java.util.concurrent.CompletableFuture;

public interface DbDataSource {
    CompletableFuture<DbConnection> connect();
    CompletableFuture<DbConnection> connect(String database);
    CompletableFuture<DbConnection> connect(String username, String password, String database);
}
