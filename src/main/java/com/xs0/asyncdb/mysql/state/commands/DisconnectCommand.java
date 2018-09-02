package com.xs0.asyncdb.mysql.state.commands;

import com.xs0.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class DisconnectCommand extends MySQLCommand {
    private static final Logger log = LoggerFactory.getLogger(DisconnectCommand.class);

    private final CompletableFuture<Void> promise;

    public DisconnectCommand(CompletableFuture<Void> promise) {
        this.promise = promise;
    }

    @Override
    public CompletableFuture<Void> getPromise() {
        return promise;
    }

    @Override
    public Result start(Support support) {
        return Result.disconnect(promise);
    }

    @Override
    public Result processPacket(ByteBuf packet, Support support) {
        log.error("DisconnectStateMachine.processPacket called");
        return Result.protocolErrorAbortEverything("DisconnectStateMachine.processPacket called, which should never happen.");
    }
}
