package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.io.PreparedStatementInfo;
import com.github.mslenc.asyncdb.my.msgclient.ClosePreparedStatementMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MyClosePreparedStatementCmd extends MyCommand {
    private static final Logger log = LoggerFactory.getLogger(MyClosePreparedStatementCmd.class);

    private final PreparedStatementInfo psInfo;
    private final CompletableFuture<Void> promise;

    public MyClosePreparedStatementCmd(MyConnection conn, PreparedStatementInfo psInfo, CompletableFuture<Void> promise) {
        super(conn);
        this.psInfo = psInfo;
        this.promise = promise;
    }

    @Override
    public CompletableFuture<Void> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        conn.sendMessage(new ClosePreparedStatementMessage(psInfo.statementId));
        promise.complete(null);
        return Result.stateMachineFinished();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        log.error("ClosePreparedStatementCommand.processPacket called");
        return Result.protocolErrorAbortEverything("ClosePreparedStatementCommand.processPacket called");
    }
}
