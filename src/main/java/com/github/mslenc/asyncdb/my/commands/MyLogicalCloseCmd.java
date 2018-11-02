package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.util.FutureUtils;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MyLogicalCloseCmd extends MyCommand {
    private static final Logger log = LoggerFactory.getLogger(MyLogicalCloseCmd.class);

    private final CompletableFuture<Void> promise;

    public MyLogicalCloseCmd(MyConnection conn, CompletableFuture<Void> promise) {
        super(conn);

        this.promise = promise;
    }

    @Override
    public CompletableFuture<?> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        FutureUtils.safelyComplete(promise, null);
        return Result.stateMachineFinished();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        log.error("MyLogicalCloseCmd.processPacket called");
        return Result.protocolErrorAbortEverything("MyLogicalCloseCmd.processPacket called, which should never happen.");
    }
}
