package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.msgclient.InitDbMessage;
import com.github.mslenc.asyncdb.my.msgserver.OkMessage;
import com.github.mslenc.asyncdb.util.FutureUtils;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.my.MyConstants.PACKET_HEADER_ERR;
import static com.github.mslenc.asyncdb.my.MyConstants.PACKET_HEADER_OK;
import static com.github.mslenc.asyncdb.my.MyConstants.consumePacketHeader;

public class MyInitDatabaseCmd extends MyCommand {
    private final CompletableFuture<Void> promise;
    private final String database;

    public MyInitDatabaseCmd(MyConnection conn, String database, CompletableFuture<Void> promise) {
        super(conn);

        this.promise = promise;
        this.database = database;
    }

    @Override
    public CompletableFuture<Void> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        conn.sendMessage(new InitDbMessage(database));
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        int header = consumePacketHeader(packet);

        switch (header) {
            case PACKET_HEADER_OK:
                OkMessage.decodeAfterHeader(packet, StandardCharsets.UTF_8);
                promise.complete(null);
                return Result.stateMachineFinished();

            case PACKET_HEADER_ERR:
                FutureUtils.failWithError(promise, conn.decodeErrorAfterHeader(packet));
                return Result.stateMachineFinished();

            default:
                return Result.unknownHeaderByte(header, "awaiting response");
        }
    }
}
