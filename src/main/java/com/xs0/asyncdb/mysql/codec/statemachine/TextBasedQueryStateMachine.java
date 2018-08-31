package com.xs0.asyncdb.mysql.codec.statemachine;

import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.common.general.MutableResultSet;
import com.xs0.asyncdb.mysql.MySQLQueryResult;
import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.codec.MySQLConnectionHandler;
import com.xs0.asyncdb.mysql.decoder.*;
import com.xs0.asyncdb.mysql.ex.MySQLException;
import com.xs0.asyncdb.mysql.message.client.QueryMessage;
import com.xs0.asyncdb.mysql.message.server.*;
import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static com.xs0.asyncdb.mysql.util.MySQLIO.PACKET_HEADER_EOF;
import static com.xs0.asyncdb.mysql.util.MySQLIO.PACKET_HEADER_ERR;
import static com.xs0.asyncdb.mysql.util.MySQLIO.consumePacketHeader;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TextBasedQueryStateMachine implements MySQLStateMachine {
    private static final int STATE_AWAITING_FIELD_COUNT = 0;
    private static final int STATE_READING_FIELDS = 1;
    private static final int STATE_READING_ROWS = 2;

    private final String query;
    private final CompletableFuture<QueryResult> promise;
    private MySQLConnectionHandler conn;
    private int state = STATE_AWAITING_FIELD_COUNT;
    private int expectedFieldCount;
    private final ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();
    private MutableResultSet<ColumnDefinitionMessage> resultSet;

    public TextBasedQueryStateMachine(String query, CompletableFuture<QueryResult> promise) {
        this.query = query;
        this.promise = promise;
    }

    @Override
    public Result init(MySQLConnectionHandler conn) {
        if (promise.isDone())
            return Result.stateMachineFinished();

        this.conn = conn;
        conn.sendMessage(new QueryMessage(query));
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        // https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-COM_QUERY_Response

        switch (state) {
            case STATE_AWAITING_FIELD_COUNT:
                return processPacket_AWAITING_FIELD_COUNT(packet);

            case STATE_READING_FIELDS:
                return processPacket_READING_FIELDS(packet);

            case STATE_READING_ROWS:
                return processPacket_READING_ROWS(packet);

            default:
                return Result.protocolErrorAbortEverything("Unexpected state (" + state + ")");
        }
    }

    private Result processPacket_AWAITING_FIELD_COUNT(ByteBuf packet) {
        if (packet.readableBytes() < 1)
            return Result.unknownHeaderByte(-1, "text query (awaiting field count)");

        int firstByte = packet.readUnsignedByte();

        switch (firstByte) {
            case MySQLIO.PACKET_HEADER_OK: // (no columns)
                OkMessage ok = OkDecoder.decodeAfterHeader(packet, UTF_8);
                // TODO: check status flags? see https://dev.mysql.com/doc/internals/en/status-flags.html
                promise.complete(new MySQLQueryResult(ok.affectedRows, ok.message, ok.lastInsertId, ok.statusFlags, ok.warnings, null));
                return Result.stateMachineFinished();

            case MySQLIO.PACKET_HEADER_ERR:
                ErrorMessage error = ErrorDecoder.decodeAfterHeader(packet, UTF_8, conn.serverInfo().serverCapabilities);
                promise.completeExceptionally(new MySQLException(error));
                return Result.stateMachineFinished();

            case MySQLIO.PACKET_HEADER_GET_MORE_CLIENT_DATA:
                return Result.protocolErrorAbortEverything("Server asked for client data, which is not supported");

            default:
                long expectedFieldCount = ByteBufUtils.readBinaryLength(firstByte, packet);
                if (expectedFieldCount < 1 || expectedFieldCount > Integer.MAX_VALUE)
                    return Result.protocolErrorAbortEverything("Received an invalid column count (" + expectedFieldCount + ")");

                this.expectedFieldCount = (int) expectedFieldCount;
                state = STATE_READING_FIELDS;
                return Result.expectingMorePackets();
        }
    }

    private Result processPacket_READING_FIELDS(ByteBuf packet) {
        // first, we expect N column definitions
        if (columnDefs.size() < expectedFieldCount) {
            ColumnDefinitionMessage columnDef = ColumnDefinitionDecoder.decode(packet, UTF_8, conn.decoderRegistry);
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

    private Result processPacket_READING_ROWS(ByteBuf packet) {
        if (!packet.isReadable())
            return Result.unknownHeaderByte(-1, "text query (reading rows)");

        int firstByte = packet.getUnsignedByte(packet.readerIndex());
        if (firstByte == MySQLIO.PACKET_HEADER_EOF) {
            // we're done :)
            packet.readByte();
            EOFMessage eof = EOFMessageDecoder.decodeAfterHeader(packet);
            promise.complete(new MySQLQueryResult(0, null, -1, eof.flags, eof.warningCount, resultSet));
            return Result.stateMachineFinished();
        }

        if (firstByte == PACKET_HEADER_ERR) {
            // we're done :(
            packet.readByte();
            ErrorMessage err = ErrorDecoder.decodeAfterHeader(packet, UTF_8, conn.serverInfo().serverCapabilities);
            promise.completeExceptionally(new MySQLException(err));
            return Result.stateMachineFinished();
        }

        ResultSetRowMessage rowMsg = ResultSetRowDecoder.decode(packet);
        resultSet.addRow(remapRow(rowMsg));
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
