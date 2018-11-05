package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.msgclient.ResetConnectionMessage;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.my.MyConstants.*;
import static com.github.mslenc.asyncdb.util.FutureUtils.failWithError;
import static com.github.mslenc.asyncdb.util.FutureUtils.safelyComplete;

public class MyResetConnectionCmd extends MyCommand {
    private final CompletableFuture<Void> promise;

    public MyResetConnectionCmd(MyConnection conn, CompletableFuture<Void> promise) {
        super(conn);
        this.promise = promise;
    }

    @Override
    public CompletableFuture<Void> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        if (promise.isDone())
            return Result.stateMachineFinished();

        conn.sendMessage(ResetConnectionMessage.instance);
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_OK:
                safelyComplete(promise, null);
                return Result.stateMachineFinished();

            case PACKET_HEADER_ERR:
                failWithError(promise, conn.decodeErrorAfterHeader(packet));
                return Result.stateMachineFinished();

            default:
                return Result.unknownHeaderByte(header, "reset connection");
        }
    }
}
