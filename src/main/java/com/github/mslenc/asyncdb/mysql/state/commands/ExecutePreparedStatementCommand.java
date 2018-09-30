package com.github.mslenc.asyncdb.mysql.state.commands;

import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import com.github.mslenc.asyncdb.common.QueryResult;
import com.github.mslenc.asyncdb.common.general.MutableResultSet;
import com.github.mslenc.asyncdb.mysql.MySQLQueryResult;
import com.github.mslenc.asyncdb.mysql.binary.BinaryRowDecoder;
import com.github.mslenc.asyncdb.mysql.binary.BinaryRowEncoder;
import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.binary.encoder.BinaryEncoder;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.codec.PreparedStatementInfo;
import com.github.mslenc.asyncdb.mysql.decoder.ColumnDefinitionDecoder;
import com.github.mslenc.asyncdb.mysql.decoder.EOFMessageDecoder;
import com.github.mslenc.asyncdb.mysql.decoder.OkDecoder;
import com.github.mslenc.asyncdb.mysql.ex.MySQLException;
import com.github.mslenc.asyncdb.mysql.message.client.PreparedStatementExecuteMessage;
import com.github.mslenc.asyncdb.mysql.message.client.ResetPreparedStatementMessage;
import com.github.mslenc.asyncdb.mysql.message.client.SendLongDataMessage;
import com.github.mslenc.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import com.github.mslenc.asyncdb.mysql.message.server.EOFMessage;
import com.github.mslenc.asyncdb.mysql.message.server.ErrorMessage;
import com.github.mslenc.asyncdb.mysql.message.server.OkMessage;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.setNullBit;
import static com.github.mslenc.asyncdb.mysql.util.MySQLIO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ExecutePreparedStatementCommand extends MySQLCommand {
    private static final int STATE_AWAITING_RESET_RESULT = 0;
    private static final int STATE_AWAITING_COLUMN_COUNT = 1;
    private static final int STATE_READING_COLUMNS = 2;
    private static final int STATE_READING_ROWS = 3;

    public static final long LONG_THRESHOLD = 1024;

    private final PreparedStatementInfo psInfo;
    private final List<Object> values;
    private final CompletableFuture<QueryResult> promise;
    private final CodecSettings codecSettings;

    private int state;

    private ArrayList<SendLongDataMessage> longValues = new ArrayList<>();
    private PreparedStatementExecuteMessage execMessage;
    private int numColumns;
    private ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();
    private MutableResultSet<ColumnDefinitionMessage> resultSet;

    public ExecutePreparedStatementCommand(PreparedStatementInfo psInfo, List<Object> values, CodecSettings codecSettings, CompletableFuture<QueryResult> promise) {
        this.psInfo = psInfo;
        this.values = values;
        this.codecSettings = codecSettings;
        this.promise = promise;

        assert values.size() == psInfo.paramDefs.size();
    }

    @Override
    public CompletableFuture<QueryResult> getPromise() {
        return promise;
    }

    @Override
    public Result start(Support support) {
        if (promise.isDone())
            return Result.stateMachineFinished();

        // https://dev.mysql.com/doc/internals/en/com-stmt-execute.html#packet-COM_STMT_EXECUTE

        BinaryRowEncoder encoders = support.getBinaryEncoders();

        int numParams = psInfo.paramDefs.size();

        byte[] nullBytes = new byte[(numParams + 7) / 8];
        ByteBuf typeBytes = Unpooled.buffer(numParams * 2 + 1);
        ByteBuf valueBytes = Unpooled.buffer();

        typeBytes.writeByte(1); // new-params-bound-flag=1

        for (int paramIndex = 0; paramIndex < numParams; paramIndex++) {
            Object value = values.get(paramIndex);
            ByteBuf longValue = packAsLongParameter(value);

            if (longValue != null) {
                // not null
                typeBytes.writeShortLE(FIELD_TYPE_LONG_BLOB);
                longValues.add(new SendLongDataMessage(this, psInfo.statementId, paramIndex, longValue));
            } else
            if (value != null) {
                // not null
                BinaryEncoder encoder;
                try {
                    encoder = encoders.encoderFor(value);
                } catch (Exception e) {
                    promise.completeExceptionally(e);
                    return Result.stateMachineFinished();
                }

                typeBytes.writeShortLE(encoder.encodesTo());
                try {
                    encoder.encode(value, valueBytes, codecSettings);
                } catch (Exception e) {
                    promise.completeExceptionally(e);
                    return Result.stateMachineFinished();
                }
            } else {
                setNullBit(0, nullBytes, paramIndex);
                typeBytes.writeShortLE(FIELD_TYPE_NULL);
            }
        }

        execMessage = new PreparedStatementExecuteMessage(this, psInfo.statementId, nullBytes, typeBytes, valueBytes);

        if (psInfo.shouldReset()) {
            state = STATE_AWAITING_RESET_RESULT;
            support.sendMessage(new ResetPreparedStatementMessage(this, psInfo.statementId));
            return Result.expectingMorePackets();
        }

        return sendDataAfterReset(support);
    }

    private Result sendDataAfterReset(Support support) {
        state = STATE_AWAITING_COLUMN_COUNT;

        for (SendLongDataMessage msg : longValues) {
            psInfo.markLongParamsSent();
            support.sendMessage(msg);
        }

        support.sendMessage(execMessage);

        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet, Support support) {
        switch (state) {
            case STATE_AWAITING_RESET_RESULT:
                return processPacket_AWAITING_RESET_RESULT(packet, support);

            case STATE_AWAITING_COLUMN_COUNT:
                return processPacket_AWAITING_COLUMN_COUNT(packet, support);

            case STATE_READING_COLUMNS:
                return processPacket_READING_COLUMNS(packet, support);

            case STATE_READING_ROWS:
                return processPacket_READING_ROWS(packet, support);

            default:
                return Result.protocolErrorAbortEverything("Unexpected state (" + state + ")");
        }
    }

    private Result processPacket_AWAITING_RESET_RESULT(ByteBuf packet, Support support) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_OK:
                return sendDataAfterReset(support);

            case MySQLIO.PACKET_HEADER_ERR:
                ErrorMessage error = support.decodeErrorAfterHeader(packet);
                promise.completeExceptionally(new MySQLException(error));
                return Result.stateMachineFinished();

            default:
                return Result.unknownHeaderByte(header, "execute prepared statement (after reset)");
        }
    }

    private Result processPacket_AWAITING_COLUMN_COUNT(ByteBuf packet, Support support) {
        int header = consumePacketHeader(packet);

        switch (header) {
            case NO_PACKET_HEADER:
                return Result.unknownHeaderByte(header, "execute prepared statement (awaiting column count)");

            case PACKET_HEADER_OK:
                // looks like we have no rows..
                OkMessage okMsg = OkDecoder.decodeAfterHeader(packet, UTF_8);
                MySQLQueryResult queryResult = new MySQLQueryResult(okMsg.affectedRows, okMsg.message, okMsg.lastInsertId, okMsg.statusFlags, okMsg.warnings, null);
                promise.complete(queryResult);
                return Result.stateMachineFinished();

            case PACKET_HEADER_ERR:
                ErrorMessage errMsg = support.decodeErrorAfterHeader(packet);
                promise.completeExceptionally(new MySQLException(errMsg));
                return Result.stateMachineFinished();
        }

        long numColumns = ByteBufUtils.readBinaryLength(header, packet);
        if (numColumns < 1 || numColumns > Integer.MAX_VALUE)
            return Result.protocolErrorAbortEverything("Invalid number of columns received");

        this.numColumns = (int)numColumns;
        state = STATE_READING_COLUMNS;
        return Result.expectingMorePackets();
    }

    private Result processPacket_READING_COLUMNS(ByteBuf packet, Support support) {
        if (isEOFPacket(packet)) {
            state = STATE_READING_ROWS;
            resultSet = new MutableResultSet<>(columnDefs);
            return Result.expectingMorePackets();
        }

        ColumnDefinitionMessage colDef = ColumnDefinitionDecoder.decode(packet, UTF_8, support.decoderRegistry());
        columnDefs.add(colDef);
        return Result.expectingMorePackets();
    }

    private Result processPacket_READING_ROWS(ByteBuf packet, Support support) {
        if (isEOFPacket(packet)) {
            EOFMessage eof = EOFMessageDecoder.decode(packet);
            MySQLQueryResult queryResult = new MySQLQueryResult(0, null, -1, eof.flags, eof.warningCount, resultSet);
            promise.complete(queryResult);
            return Result.stateMachineFinished();
        }

        if (isERRPacket(packet)) {
            packet.readByte();
            ErrorMessage error = support.decodeErrorAfterHeader(packet);
            promise.completeExceptionally(new MySQLException(error));
            return Result.stateMachineFinished();
        }

        Object[] values = BinaryRowDecoder.instance().decode(packet, columnDefs, codecSettings);
        resultSet.addRow(values);
        return Result.expectingMorePackets();
    }

    static ByteBuf packAsLongParameter(Object value) {
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            if (bytes.length >= LONG_THRESHOLD) {
                return Unpooled.wrappedBuffer(bytes);
            } else {
                return null;
            }
        }

        if (value instanceof ByteBuf) {
            ByteBuf bytes = (ByteBuf) value;
            if (bytes.readableBytes() >= LONG_THRESHOLD) {
                return bytes.slice();
            } else {
                return null;
            }
        }

        if (value instanceof ByteBuffer) {
            ByteBuffer bytes = (ByteBuffer) value;
            if (bytes.remaining() >= LONG_THRESHOLD) {
                return Unpooled.wrappedBuffer(bytes);
            } else {
                return null;
            }
        }

        return null;
    }
}
