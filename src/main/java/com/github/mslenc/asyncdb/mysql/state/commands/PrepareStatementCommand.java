package com.github.mslenc.asyncdb.mysql.state.commands;

import com.github.mslenc.asyncdb.common.PreparedStatement;
import com.github.mslenc.asyncdb.mysql.codec.PreparedStatementInfo;
import com.github.mslenc.asyncdb.mysql.decoder.ColumnDefinitionDecoder;
import com.github.mslenc.asyncdb.mysql.decoder.EOFMessageDecoder;
import com.github.mslenc.asyncdb.mysql.decoder.PreparedStatementPrepareResponseDecoder;
import com.github.mslenc.asyncdb.mysql.ex.MySQLException;
import com.github.mslenc.asyncdb.mysql.message.client.PreparedStatementPrepareMessage;
import com.github.mslenc.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import com.github.mslenc.asyncdb.mysql.message.server.EOFMessage;
import com.github.mslenc.asyncdb.mysql.message.server.ErrorMessage;
import com.github.mslenc.asyncdb.mysql.message.server.PreparedStatementPrepareResponse;
import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.mysql.util.MySQLIO.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;

public class PrepareStatementCommand extends MySQLCommand {
    private static final int STATE_AWAITING_PREPARE_OK = 1;
    private static final int STATE_READING_PARAMS = 2;
    private static final int STATE_READING_COLUMNS = 3;

    private final String query;
    private final CompletableFuture<PreparedStatement> promise;

    private int state = STATE_AWAITING_PREPARE_OK;
    private PreparedStatementPrepareResponse initialResponse;
    private ArrayList<ColumnDefinitionMessage> paramDefs = new ArrayList<>();
    private ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();

    public PrepareStatementCommand(String query, CompletableFuture<PreparedStatement> promise) {
        this.query = query;
        this.promise = promise;
    }

    @Override
    public CompletableFuture<PreparedStatement> getPromise() {
        return promise;
    }

    @Override
    public Result start(Support support) {
        if (promise.isDone())
            return Result.stateMachineFinished();

        support.sendMessage(new PreparedStatementPrepareMessage(this, query));
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet, Support support) {
        switch (state) {
            case STATE_AWAITING_PREPARE_OK:
                return processPacket_AWAITING_PREPARE_OK(packet, support);
            case STATE_READING_PARAMS:
                return processPacket_READING_PARAMS(packet, support);
            case STATE_READING_COLUMNS:
                return processPacket_READING_COLUMNS(packet, support);
            default:
                return Result.protocolErrorAbortEverything("Unexpected state (" + state + ")");
        }
    }

    private Result processPacket_AWAITING_PREPARE_OK(ByteBuf packet, Support support) {
        int header = consumePacketHeader(packet);

        switch (header) {
            case PACKET_HEADER_OK: // (but it is a different ok..)
                this.initialResponse = PreparedStatementPrepareResponseDecoder.decodeAfterHeader(packet);
                if (initialResponse.paramsCount > 0) {
                    state = STATE_READING_PARAMS;
                    return Result.expectingMorePackets();
                } else
                if (initialResponse.columnsCount > 0) {
                    state = STATE_READING_COLUMNS;
                    return Result.expectingMorePackets();
                } else {
                    return finish(emptyList(), emptyList(), support);
                }

            case PACKET_HEADER_ERR:
                ErrorMessage err = support.decodeErrorAfterHeader(packet);
                promise.completeExceptionally(new MySQLException(err));
                return Result.stateMachineFinished();

            default:
                return Result.unknownHeaderByte(header, "awaiting prepare");
        }
    }

    private Result processPacket_READING_PARAMS(ByteBuf packet, Support support) {
        // first, we expect N column definitions
        if (paramDefs.size() < initialResponse.paramsCount) {
            ColumnDefinitionMessage columnDef = ColumnDefinitionDecoder.decode(packet, UTF_8, support.decoderRegistry());
            paramDefs.add(columnDef);
            return Result.expectingMorePackets();
        }

        // otherwise, we expect an EOF, so we can then move on
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_EOF:
                EOFMessage eof = EOFMessageDecoder.decodeAfterHeader(packet);
                // TODO: need to check status flags?

                if (initialResponse.columnsCount > 0) {
                    state = STATE_READING_COLUMNS;
                    return Result.expectingMorePackets();
                } else {
                    return finish(paramDefs, columnDefs, support);
                }

            default:
                return Result.unknownHeaderByte(header, "text query (reading fields)");
        }
    }

    private Result processPacket_READING_COLUMNS(ByteBuf packet, Support support) {
        // first, we expect N column definitions
        if (columnDefs.size() < initialResponse.columnsCount) {
            ColumnDefinitionMessage columnDef = ColumnDefinitionDecoder.decode(packet, UTF_8, support.decoderRegistry());
            columnDefs.add(columnDef);
            return Result.expectingMorePackets();
        }

        // otherwise, we expect an EOF, so we can then move on
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_EOF:
                EOFMessage eof = EOFMessageDecoder.decodeAfterHeader(packet);
                // TODO: need to check status flags?
                return finish(paramDefs, columnDefs, support);

            default:
                return Result.unknownHeaderByte(header, "text query (reading fields)");
        }
    }


    private Result finish(List<ColumnDefinitionMessage> paramDefs, List<ColumnDefinitionMessage> columnDefs, Support support) {
        PreparedStatementInfo info = new PreparedStatementInfo(initialResponse.statementId, paramDefs, columnDefs);
        PreparedStatement statement = support.createPreparedStatement(query, info);
        promise.complete(statement);
        return Result.stateMachineFinished();
    }
}