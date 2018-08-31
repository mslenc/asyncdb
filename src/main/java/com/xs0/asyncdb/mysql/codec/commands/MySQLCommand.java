package com.xs0.asyncdb.mysql.codec.commands;

import com.xs0.asyncdb.mysql.codec.statemachine.MySQLStateMachine;

import java.util.concurrent.CompletableFuture;

public interface MySQLCommand {
    MySQLStateMachine createStateMachine();
    CompletableFuture<?> getPromise();
}
