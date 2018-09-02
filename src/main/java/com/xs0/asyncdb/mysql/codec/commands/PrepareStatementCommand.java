package com.xs0.asyncdb.mysql.codec.commands;

import com.xs0.asyncdb.common.PreparedStatement;
import com.xs0.asyncdb.mysql.codec.statemachine.MySQLStateMachine;
import com.xs0.asyncdb.mysql.codec.statemachine.PrepareStatementStateMachine;

import java.util.concurrent.CompletableFuture;

public class PrepareStatementCommand implements MySQLCommand {
    private final String query;
    private final CompletableFuture<PreparedStatement> promise;

    public PrepareStatementCommand(String query, CompletableFuture<PreparedStatement> promise) {
        this.query = query;
        this.promise = promise;
    }

    @Override
    public MySQLStateMachine createStateMachine() {
        return new PrepareStatementStateMachine(query, promise);
    }

    @Override
    public CompletableFuture<?> getPromise() {
        return promise;
    }
}
