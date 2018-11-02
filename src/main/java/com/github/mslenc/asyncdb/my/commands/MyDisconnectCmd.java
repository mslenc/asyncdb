package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MyDisconnectCmd extends MyCommand {
    private static final Logger log = LoggerFactory.getLogger(MyDisconnectCmd.class);

    private final CompletableFuture<Void> promise;

    public MyDisconnectCmd(MyConnection conn, CompletableFuture<Void> promise) {
        super(conn);
        this.promise = promise;
    }

    @Override
    public CompletableFuture<Void> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        return Result.disconnect(promise);
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        log.error("DisconnectStateMachine.processPacket called");
        return Result.protocolErrorAbortEverything("DisconnectStateMachine.processPacket called, which should never happen.");
    }
}
