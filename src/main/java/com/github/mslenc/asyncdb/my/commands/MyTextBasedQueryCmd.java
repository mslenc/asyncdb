package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.ex.BufferNotFullyConsumedException;
import com.github.mslenc.asyncdb.ex.ProtocolException;
import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.util.FutureUtils;
import com.github.mslenc.asyncdb.my.MyDbColumn;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilder;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilderFactory;
import com.github.mslenc.asyncdb.ex.DecodingException;
import com.github.mslenc.asyncdb.my.msgserver.*;
import com.github.mslenc.asyncdb.my.msgclient.QueryMessage;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.my.MyConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MyTextBasedQueryCmd<QR> extends MyCommand {
    private static final int STATE_AWAITING_FIELD_COUNT = 0;
    private static final int STATE_READING_FIELDS = 1;
    private static final int STATE_READING_ROWS = 2;

    private final ByteBuf queryUtf8;
    private final CompletableFuture<QR> promise;
    private final MyEncoders encoders;

    private int state = STATE_AWAITING_FIELD_COUNT;
    private int expectedColumnCount;
    private final ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();
    private final MyResultSetBuilderFactory<QR> rsBuilderFactory;
    private MyResultSetBuilder<QR> resultSet;

    public MyTextBasedQueryCmd(MyConnection conn, ByteBuf queryUtf8, CompletableFuture<QR> promise, MyEncoders encoders, MyResultSetBuilderFactory<QR> rsBuilderFactory) {
        super(conn);
        this.queryUtf8 = queryUtf8;
        this.promise = promise;
        this.encoders = encoders;
        this.rsBuilderFactory = rsBuilderFactory;
    }

    @Override
    public CompletableFuture<QR> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        if (promise.isDone())
            return Result.stateMachineFinished();

        conn.sendMessage(new QueryMessage(queryUtf8));
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
        int header = consumePacketHeader(packet);
        switch (header) {
            case MyConstants.NO_PACKET_HEADER:
                return Result.unknownHeaderByte(-1, "text query (awaiting field count)");

            case MyConstants.PACKET_HEADER_OK: // (no columns)
                OkMessage ok = OkMessage.decodeAfterHeader(packet, UTF_8);
                // TODO: check status flags? see https://dev.mysql.com/doc/internals/en/status-flags.html
                promise.complete(rsBuilderFactory.makeQueryResultWithNoRows(ok.affectedRows, ok.message, ok.lastInsertId, ok.statusFlags, ok.warnings));
                return Result.stateMachineFinished();

            case MyConstants.PACKET_HEADER_ERR:
                FutureUtils.failWithError(promise, conn.decodeErrorAfterHeader(packet));
                return Result.stateMachineFinished();

            case MyConstants.PACKET_HEADER_GET_MORE_CLIENT_DATA:
                return Result.protocolErrorAbortEverything("Server asked for client data, which is not supported");

            default:
                long expectedColumnCount = ByteBufUtils.readBinaryLength(header, packet);
                if (expectedColumnCount < 1 || expectedColumnCount > Integer.MAX_VALUE)
                    return Result.protocolErrorAbortEverything("Received an invalid column count (" + expectedColumnCount + ")");

                this.expectedColumnCount = (int) expectedColumnCount;
                state = STATE_READING_FIELDS;
                return Result.expectingMorePackets();
        }
    }

    private Result processPacket_READING_FIELDS(ByteBuf packet) {
        // first, we expect N column definitions
        if (columnDefs.size() < expectedColumnCount) {
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
                resultSet = rsBuilderFactory.makeResultSetBuilder(encoders, MyDbColumns.create(columnDefs));
                state = STATE_READING_ROWS;
                return Result.expectingMorePackets();

            default:
                return Result.unknownHeaderByte(header, "text query (reading fields)");
        }
    }

    private Result processPacket_READING_ROWS(ByteBuf packet) {
        if (!packet.isReadable())
            return Result.unknownHeaderByte(-1, "text query (reading rows)");

        if (isEOFPacket(packet)) {
            // we're done :)
            EOFMessage eof = EOFMessage.decode(packet);
            promise.complete(resultSet.build(eof.flags, eof.warningCount));
            return Result.stateMachineFinished();
        }

        if (isERRPacket(packet)) {
            // we're done :(
            packet.readByte(); // (skip the header byte)
            FutureUtils.failWithError(promise, conn.decodeErrorAfterHeader(packet));
            return Result.stateMachineFinished();
        }

        return readRow(packet);
    }

    private Result readRow(ByteBuf rowPacket) {
        try {
            resultSet.startRow();
        } catch (Throwable t) {
            return Result.protocolErrorAbortEverything(t);
        }

        for (int i = 0; i < expectedColumnCount; i++) {
            MyDbColumn column = columnDefs.get(i);

            if (rowPacket.readableBytes() < 1)
                return Result.protocolErrorAbortEverything(new ProtocolException("Incomplete row data"));

            int firstByte = rowPacket.readUnsignedByte();

            if (firstByte == TEXT_RESULTSET_NULL) {
                try {
                    resultSet.nullValue(column);
                } catch (Throwable t) {
                    return Result.protocolErrorAbortEverything(t);
                }
                continue;
            }

            long longLength = ByteBufUtils.readBinaryLength(firstByte, rowPacket);
            if (longLength < 0 || longLength > rowPacket.readableBytes())
                return Result.protocolErrorAbortEverything(new ProtocolException("Value length negative or exceeds packet size"));

            int length = (int) longLength;
            int expectReadableAfter = rowPacket.readableBytes() - length;

            try {
                resultSet.textValue(column, rowPacket, length);
            } catch (Throwable t) {
                return Result.protocolErrorAbortEverything(t);
            }

            if (rowPacket.readableBytes() != expectReadableAfter) {
                return Result.protocolErrorAbortEverything(new DecodingException("The result set builder failed to read all the bytes it should have."));
            }
        }

        if (rowPacket.readableBytes() > 0)
            return Result.protocolErrorAbortEverything(new BufferNotFullyConsumedException(rowPacket));

        resultSet.endRow();

        return Result.expectingMorePackets();
    }
}
