package com.github.mslenc.asyncdb.mysql.state.commands;

import com.github.mslenc.asyncdb.common.QueryResult;
import com.github.mslenc.asyncdb.common.general.MutableResultSet;
import com.github.mslenc.asyncdb.mysql.MySQLQueryResult;
import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.decoder.ColumnDefinitionDecoder;
import com.github.mslenc.asyncdb.mysql.decoder.EOFMessageDecoder;
import com.github.mslenc.asyncdb.mysql.decoder.OkDecoder;
import com.github.mslenc.asyncdb.mysql.decoder.ResultSetRowDecoder;
import com.github.mslenc.asyncdb.mysql.message.server.*;
import com.github.mslenc.asyncdb.mysql.ex.MySQLException;
import com.github.mslenc.asyncdb.mysql.message.client.QueryMessage;
import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.mysql.util.MySQLIO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TextBasedQueryCommand extends MySQLCommand {
    private static final int STATE_AWAITING_FIELD_COUNT = 0;
    private static final int STATE_READING_FIELDS = 1;
    private static final int STATE_READING_ROWS = 2;

    private final String query;
    private final CompletableFuture<QueryResult> promise;
    private int state = STATE_AWAITING_FIELD_COUNT;
    private int expectedFieldCount;
    private final ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();
    private MutableResultSet<ColumnDefinitionMessage> resultSet;

    public TextBasedQueryCommand(String query, CompletableFuture<QueryResult> promise) {
        this.query = query;
        this.promise = promise;
    }

    @Override
    public CompletableFuture<QueryResult> getPromise() {
        return promise;
    }

    @Override
    public Result start(Support conn) {
        if (promise.isDone())
            return Result.stateMachineFinished();

        conn.sendMessage(new QueryMessage(this, query));
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet, Support support) {
        // https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-COM_QUERY_Response

        switch (state) {
            case STATE_AWAITING_FIELD_COUNT:
                return processPacket_AWAITING_FIELD_COUNT(packet, support);

            case STATE_READING_FIELDS:
                return processPacket_READING_FIELDS(packet, support);

            case STATE_READING_ROWS:
                return processPacket_READING_ROWS(packet, support);

            default:
                return Result.protocolErrorAbortEverything("Unexpected state (" + state + ")");
        }
    }

    private Result processPacket_AWAITING_FIELD_COUNT(ByteBuf packet, Support support) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case MySQLIO.NO_PACKET_HEADER:
                return Result.unknownHeaderByte(-1, "text query (awaiting field count)");

            case MySQLIO.PACKET_HEADER_OK: // (no columns)
                OkMessage ok = OkDecoder.decodeAfterHeader(packet, UTF_8);
                // TODO: check status flags? see https://dev.mysql.com/doc/internals/en/status-flags.html
                promise.complete(new MySQLQueryResult(ok.affectedRows, ok.message, ok.lastInsertId, ok.statusFlags, ok.warnings, null));
                return Result.stateMachineFinished();

            case MySQLIO.PACKET_HEADER_ERR:
                ErrorMessage error = support.decodeErrorAfterHeader(packet);
                promise.completeExceptionally(new MySQLException(error));
                return Result.stateMachineFinished();

            case MySQLIO.PACKET_HEADER_GET_MORE_CLIENT_DATA:
                return Result.protocolErrorAbortEverything("Server asked for client data, which is not supported");

            default:
                long expectedFieldCount = ByteBufUtils.readBinaryLength(header, packet);
                if (expectedFieldCount < 1 || expectedFieldCount > Integer.MAX_VALUE)
                    return Result.protocolErrorAbortEverything("Received an invalid column count (" + expectedFieldCount + ")");

                this.expectedFieldCount = (int) expectedFieldCount;
                state = STATE_READING_FIELDS;
                return Result.expectingMorePackets();
        }
    }

    private Result processPacket_READING_FIELDS(ByteBuf packet, Support support) {
        // first, we expect N column definitions
        if (columnDefs.size() < expectedFieldCount) {
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
                resultSet = new MutableResultSet<>(columnDefs);
                state = STATE_READING_ROWS;
                return Result.expectingMorePackets();

            default:
                return Result.unknownHeaderByte(header, "text query (reading fields)");
        }
    }

    private Result processPacket_READING_ROWS(ByteBuf packet, Support support) {
        if (!packet.isReadable())
            return Result.unknownHeaderByte(-1, "text query (reading rows)");

        if (isEOFPacket(packet)) {
            // we're done :)
            EOFMessage eof = EOFMessageDecoder.decode(packet);
            promise.complete(new MySQLQueryResult(0, null, -1, eof.flags, eof.warningCount, resultSet));
            return Result.stateMachineFinished();
        }

        if (isERRPacket(packet)) {
            // we're done :(
            packet.readByte(); // (skip the header byte)
            ErrorMessage err = support.decodeErrorAfterHeader(packet);
            promise.completeExceptionally(new MySQLException(err));
            return Result.stateMachineFinished();
        }

        ResultSetRowMessage rowMsg = ResultSetRowDecoder.decode(packet);
        resultSet.addRow(remapRow(rowMsg));
        ResultSetRowDecoder.releaseBufs(rowMsg);


        return Result.expectingMorePackets();
    }

    private Object[] remapRow(ResultSetRowMessage rowMsg) {
        Object[] items = new Object[rowMsg.size()];

        int x = 0;
        while (x < rowMsg.size()) {
            if (rowMsg.get(x) == null) {
                items[x] = null;
            } else {
                ColumnDefinitionMessage colDef = columnDefs.get(x);
                items[x] = colDef.textDecoder.decode(colDef, rowMsg.get(x), UTF_8);
            }
            x++;
        }

        return items;
    }
}
