package com.xs0.asyncdb.mysql.codec.commands;

import com.xs0.asyncdb.mysql.codec.statemachine.DisconnectStateMachine;
import com.xs0.asyncdb.mysql.codec.statemachine.MySQLStateMachine;

import java.util.concurrent.CompletableFuture;

public class DisconnectCommand implements MySQLCommand {
    private final CompletableFuture<Void> promise;

    public DisconnectCommand(CompletableFuture<Void> promise) {
        this.promise = promise;
    }

    @Override
    public MySQLStateMachine createStateMachine() {
        return new DisconnectStateMachine(promise);
    }

    @Override
    public CompletableFuture<?> getPromise() {
        return promise;
    }
}
