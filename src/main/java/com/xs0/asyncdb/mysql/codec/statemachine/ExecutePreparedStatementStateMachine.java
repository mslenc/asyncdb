package com.xs0.asyncdb.mysql.codec.statemachine;

import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.common.general.MutableResultSet;
import com.xs0.asyncdb.mysql.MySQLQueryResult;
import com.xs0.asyncdb.mysql.binary.BinaryRowDecoder;
import com.xs0.asyncdb.mysql.binary.BinaryRowEncoder;
import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.binary.encoder.BinaryEncoder;
import com.xs0.asyncdb.mysql.codec.PreparedStatementInfo;
import com.xs0.asyncdb.mysql.decoder.ColumnDefinitionDecoder;
import com.xs0.asyncdb.mysql.decoder.EOFMessageDecoder;
import com.xs0.asyncdb.mysql.decoder.OkDecoder;
import com.xs0.asyncdb.mysql.ex.MySQLException;
import com.xs0.asyncdb.mysql.message.client.PreparedStatementExecuteMessage;
import com.xs0.asyncdb.mysql.message.client.ResetPreparedStatementMessage;
import com.xs0.asyncdb.mysql.message.client.SendLongDataMessage;
import com.xs0.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import com.xs0.asyncdb.mysql.message.server.EOFMessage;
import com.xs0.asyncdb.mysql.message.server.ErrorMessage;
import com.xs0.asyncdb.mysql.message.server.OkMessage;
import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.setNullBit;
import static com.xs0.asyncdb.mysql.column.ColumnType.FIELD_TYPE_LONG_BLOB;
import static com.xs0.asyncdb.mysql.column.ColumnType.FIELD_TYPE_NULL;
import static com.xs0.asyncdb.mysql.util.MySQLIO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ExecutePreparedStatementStateMachine implements MySQLStateMachine {
    private static final int STATE_AWAITING_RESET_RESULT = 0;
    private static final int STATE_AWAITING_COLUMN_COUNT = 1;
    private static final int STATE_READING_COLUMNS = 2;
    private static final int STATE_READING_ROWS = 3;

    public static final long LONG_THRESHOLD = 1024;

    private final PreparedStatementInfo psInfo;
    private final List<Object> values;
    private final CompletableFuture<QueryResult> promise;

    private int state;

    private ArrayList<SendLongDataMessage> longValues = new ArrayList<>();
    private PreparedStatementExecuteMessage execMessage;
    private int numColumns;
    private ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();
    private MutableResultSet<ColumnDefinitionMessage> resultSet;

    public ExecutePreparedStatementStateMachine(PreparedStatementInfo psInfo, List<Object> values, CompletableFuture<QueryResult> promise) {
        this.psInfo = psInfo;
        this.values = values;
        this.promise = promise;

        assert values.size() == psInfo.paramDefs.size();
    }

    @Override
    public Result start(Support support) {
        // https://dev.mysql.com/doc/internals/en/com-stmt-execute.html#packet-COM_STMT_EXECUTE

        BinaryRowEncoder encoders = support.getBinaryEncoders();

        int numParams = psInfo.paramDefs.size();

        byte[] nullBytes = new byte[(numParams + 7) / 8];
        ByteBuf typeBytes = newMysqlBuffer(numParams * 2 + 1);
        ByteBuf valueBytes = newMysqlBuffer();

        typeBytes.writeByte(1); // new-params-bound-flag=1

        for (int paramIndex = 0; paramIndex < numParams; paramIndex++) {
            Object value = values.get(paramIndex);
            ByteBuf longValue = packAsLongParameter(value);

            if (longValue != null) {
                // not null
                typeBytes.writeShortLE(FIELD_TYPE_LONG_BLOB);
                longValues.add(new SendLongDataMessage(psInfo.statementId, paramIndex, longValue));
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
                    encoder.encode(value, valueBytes);
                } catch (Exception e) {
                    promise.completeExceptionally(e);
                    return Result.stateMachineFinished();
                }
            } else {
                setNullBit(0, nullBytes, paramIndex);
                typeBytes.writeShortLE(FIELD_TYPE_NULL);
            }
        }

        execMessage = new PreparedStatementExecuteMessage(psInfo.statementId, nullBytes, typeBytes, valueBytes);

        if (psInfo.shouldReset()) {
            state = STATE_AWAITING_RESET_RESULT;
            support.sendMessage(new ResetPreparedStatementMessage(psInfo.statementId));
            return Result.expectingMorePackets();
        }

        return sendDataAfterReset(support);
    }

    private Result sendDataAfterReset(Support support) {
        state = STATE_AWAITING_COLUMN_COUNT;

        for (SendLongDataMessage msg : longValues)
            support.sendMessage(msg);

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

        Object[] values = BinaryRowDecoder.instance().decode(packet, columnDefs);
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
