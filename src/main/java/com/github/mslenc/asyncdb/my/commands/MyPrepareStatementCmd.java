package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.util.FutureUtils;
import com.github.mslenc.asyncdb.my.MyPreparedStatement;
import com.github.mslenc.asyncdb.my.io.PreparedStatementInfo;
import com.github.mslenc.asyncdb.my.msgclient.PreparedStatementPrepareMessage;
import com.github.mslenc.asyncdb.my.msgserver.ColumnDefinitionMessage;
import com.github.mslenc.asyncdb.my.msgserver.EOFMessage;
import com.github.mslenc.asyncdb.my.msgserver.PreparedStatementPrepareResponse;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.my.MyConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;

public class MyPrepareStatementCmd extends MyCommand {
    private static final int STATE_AWAITING_PREPARE_OK = 1;
    private static final int STATE_READING_PARAMS = 2;
    private static final int STATE_READING_COLUMNS = 3;

    private final String query;
    private final CompletableFuture<MyPreparedStatement> promise;

    private int state = STATE_AWAITING_PREPARE_OK;
    private PreparedStatementPrepareResponse initialResponse;
    private ArrayList<ColumnDefinitionMessage> paramDefs = new ArrayList<>();
    private ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();

    public MyPrepareStatementCmd(MyConnection conn, String query, CompletableFuture<MyPreparedStatement> promise) {
        super(conn);
        this.query = query;
        this.promise = promise;
    }

    @Override
    public CompletableFuture<MyPreparedStatement> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        if (promise.isDone())
            return Result.stateMachineFinished();

        conn.sendMessage(new PreparedStatementPrepareMessage(query));
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        switch (state) {
            case STATE_AWAITING_PREPARE_OK:
                return processPacket_AWAITING_PREPARE_OK(packet);
            case STATE_READING_PARAMS:
                return processPacket_READING_PARAMS(packet);
            case STATE_READING_COLUMNS:
                return processPacket_READING_COLUMNS(packet);
            default:
                return Result.protocolErrorAbortEverything("Unexpected state (" + state + ")");
        }
    }

    private Result processPacket_AWAITING_PREPARE_OK(ByteBuf packet) {
        int header = consumePacketHeader(packet);

        switch (header) {
            case PACKET_HEADER_OK: // (but it is a different ok..)
                this.initialResponse = PreparedStatementPrepareResponse.decodeAfterHeader(packet);
                if (initialResponse.paramsCount > 0) {
                    state = STATE_READING_PARAMS;
                    return Result.expectingMorePackets();
                } else
                if (initialResponse.columnsCount > 0) {
                    state = STATE_READING_COLUMNS;
                    return Result.expectingMorePackets();
                } else {
                    return finish(emptyList(), emptyList());
                }

            case PACKET_HEADER_ERR:
                FutureUtils.failWithError(promise, conn.decodeErrorAfterHeader(packet));
                return Result.stateMachineFinished();

            default:
                return Result.unknownHeaderByte(header, "awaiting prepare");
        }
    }

    private Result processPacket_READING_PARAMS(ByteBuf packet) {
        // first, we expect N column definitions
        if (paramDefs.size() < initialResponse.paramsCount) {
            ColumnDefinitionMessage columnDef = ColumnDefinitionMessage.decode(paramDefs.size(), packet, UTF_8);
            paramDefs.add(columnDef);
            return Result.expectingMorePackets();
        }

        // otherwise, we expect an EOF, so we can then move on
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_EOF:
                EOFMessage eof = EOFMessage.decodeAfterHeader(packet);
                // TODO: need to check status flags?

                if (initialResponse.columnsCount > 0) {
                    state = STATE_READING_COLUMNS;
                    return Result.expectingMorePackets();
                } else {
                    return finish(paramDefs, columnDefs);
                }

            default:
                return Result.unknownHeaderByte(header, "text query (reading fields)");
        }
    }

    private Result processPacket_READING_COLUMNS(ByteBuf packet) {
        // first, we expect N column definitions
        if (columnDefs.size() < initialResponse.columnsCount) {
            ColumnDefinitionMessage columnDef = ColumnDefinitionMessage.decode(columnDefs.size(), packet, UTF_8);
            columnDefs.add(columnDef);
            return Result.expectingMorePackets();
        }

        // otherwise, we expect an EOF, so we can then move on
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_EOF:
                EOFMessage eof = EOFMessage.decodeAfterHeader(packet);
                // TODO: need to check status flags?
                return finish(paramDefs, columnDefs);

            default:
                return Result.unknownHeaderByte(header, "text query (reading fields)");
        }
    }


    private Result finish(List<ColumnDefinitionMessage> paramDefs, List<ColumnDefinitionMessage> columnDefs) {
        PreparedStatementInfo info = new PreparedStatementInfo(initialResponse.statementId, paramDefs, columnDefs);
        MyPreparedStatement statement = new MyPreparedStatement(conn, query, info);
        promise.complete(statement);
        return Result.stateMachineFinished();
    }
}
