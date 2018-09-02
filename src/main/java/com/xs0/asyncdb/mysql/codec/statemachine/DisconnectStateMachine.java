package com.xs0.asyncdb.mysql.codec.statemachine;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class DisconnectStateMachine implements MySQLStateMachine {
    private static final Logger log = LoggerFactory.getLogger(DisconnectStateMachine.class);

    private final CompletableFuture<Void> promise;

    public DisconnectStateMachine(CompletableFuture<Void> promise) {
        this.promise = promise;
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
