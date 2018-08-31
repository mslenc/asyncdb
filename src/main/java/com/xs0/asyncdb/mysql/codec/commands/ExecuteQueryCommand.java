package com.xs0.asyncdb.mysql.codec.commands;

import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.mysql.codec.statemachine.MySQLStateMachine;
import com.xs0.asyncdb.mysql.codec.statemachine.TextBasedQueryStateMachine;

import java.util.concurrent.CompletableFuture;

public class ExecuteQueryCommand implements MySQLCommand {
    private final String query;
    private final CompletableFuture<QueryResult> promise;

    public ExecuteQueryCommand(String query, CompletableFuture<QueryResult> promise) {
        this.query = query;
        this.promise = promise;
    }

    @Override
    public MySQLStateMachine createStateMachine() {
        return new TextBasedQueryStateMachine(query, promise);
    }

    @Override
    public CompletableFuture<?> getPromise() {
        return promise;
    }
}
