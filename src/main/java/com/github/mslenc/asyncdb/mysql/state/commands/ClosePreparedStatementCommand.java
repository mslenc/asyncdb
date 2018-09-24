package com.github.mslenc.asyncdb.mysql.state.commands;

import com.github.mslenc.asyncdb.mysql.codec.PreparedStatementInfo;
import com.github.mslenc.asyncdb.mysql.message.client.ClosePreparedStatementMessage;
import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ClosePreparedStatementCommand extends MySQLCommand {
    private static final Logger log = LoggerFactory.getLogger(ClosePreparedStatementCommand.class);

    private final PreparedStatementInfo psInfo;
    private final CompletableFuture<Void> promise;

    public ClosePreparedStatementCommand(PreparedStatementInfo psInfo, CompletableFuture<Void> promise) {
        this.psInfo = psInfo;
        this.promise = promise;
    }

    @Override
    public CompletableFuture<Void> getPromise() {
        return promise;
    }

    @Override
    public Result start(Support support) {
        support.sendMessage(new ClosePreparedStatementMessage(this, psInfo.statementId));
        promise.complete(null);
        return Result.stateMachineFinished();
    }

    @Override
    public Result processPacket(ByteBuf packet, Support support) {
        log.error("ClosePreparedStatementCommand.processPacket called");
        return Result.protocolErrorAbortEverything("ClosePreparedStatementCommand.processPacket called");
    }
}
