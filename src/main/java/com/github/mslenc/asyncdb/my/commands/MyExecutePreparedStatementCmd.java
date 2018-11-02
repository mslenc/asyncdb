package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.util.FutureUtils;
import com.github.mslenc.asyncdb.ex.BufferNotFullyConsumedException;
import com.github.mslenc.asyncdb.my.MyDbColumn;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyValueEncoder;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilder;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilderFactory;
import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.io.PreparedStatementInfo;
import com.github.mslenc.asyncdb.my.msgclient.PreparedStatementExecuteMessage;
import com.github.mslenc.asyncdb.my.msgclient.ResetPreparedStatementMessage;
import com.github.mslenc.asyncdb.my.msgclient.SendLongDataMessage;
import com.github.mslenc.asyncdb.my.msgserver.ColumnDefinitionMessage;
import com.github.mslenc.asyncdb.my.msgserver.EOFMessage;
import com.github.mslenc.asyncdb.my.msgserver.OkMessage;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.setNullBit;
import static com.github.mslenc.asyncdb.my.MyConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MyExecutePreparedStatementCmd<QR> extends MyCommand {
    private static final int STATE_AWAITING_RESET_RESULT = 0;
    private static final int STATE_AWAITING_COLUMN_COUNT = 1;
    private static final int STATE_READING_COLUMNS = 2;
    private static final int STATE_READING_ROWS = 3;

    private final PreparedStatementInfo psInfo;
    private final List<Object> values;
    private final CompletableFuture<QR> promise;
    private final MyEncoders encoders;
    private final MyResultSetBuilderFactory<QR> rsFactory;

    private int state;

    private ArrayList<SendLongDataMessage> longValues = new ArrayList<>();
    private PreparedStatementExecuteMessage execMessage;
    private ArrayList<ColumnDefinitionMessage> columnDefs = new ArrayList<>();
    private MyResultSetBuilder<QR> resultSetBuilder;

    public MyExecutePreparedStatementCmd(MyConnection conn, PreparedStatementInfo psInfo, List<Object> values, MyEncoders encoders, MyResultSetBuilderFactory<QR> rsFactory, CompletableFuture<QR> promise) {
        super(conn);
        this.psInfo = psInfo;
        this.values = values;
        this.encoders = encoders;
        this.rsFactory = rsFactory;
        this.promise = promise;

        assert values.size() == psInfo.getParameters().size();
    }

    @Override
    public CompletableFuture<QR> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        if (promise.isDone())
            return Result.stateMachineFinished();

        // https://dev.mysql.com/doc/internals/en/com-stmt-execute.html#packet-COM_STMT_EXECUTE

        int numParams = psInfo.paramDefs.size();

        byte[] nullBytes = new byte[(numParams + 7) / 8];
        ByteBuf typeBytes = Unpooled.buffer(numParams * 2 + 1);
        ByteBuf valueBytes = Unpooled.buffer();

        typeBytes.writeByte(1); // new-params-bound-flag=1

        for (int paramIndex = 0; paramIndex < numParams; paramIndex++) {
            Object value = values.get(paramIndex);

            try {
                prepareValue(paramIndex, value, nullBytes, typeBytes, valueBytes);
            } catch (Exception e) {
                return Result.protocolErrorAbortEverything(e);
            }
        }

        execMessage = new PreparedStatementExecuteMessage(psInfo.statementId, nullBytes, typeBytes, valueBytes);

        if (psInfo.shouldReset()) {
            state = STATE_AWAITING_RESET_RESULT;
            conn.sendMessage(new ResetPreparedStatementMessage(psInfo.statementId));
            return Result.expectingMorePackets();
        }

        return sendDataAfterReset();
    }

    private <T> void prepareValue(int paramIndex, T value, byte[] nullBytes, ByteBuf typeBytes, ByteBuf valueBytes) {
        if (value == null) {
            setNullBit(0, nullBytes, paramIndex);
            typeBytes.writeShortLE(FIELD_TYPE_NULL);
            return;
        }

        MyValueEncoder<? super T> encoder = encoders.encoderFor(value);
        if (encoder.isNull(value, encoders)) {
            setNullBit(0, nullBytes, paramIndex);
            typeBytes.writeShortLE(FIELD_TYPE_NULL);
            return;
        }

        typeBytes.writeShortLE(encoder.binaryFieldType(value, encoders));

        if (encoder.isLongBinaryValue(value, encoders)) {
            ByteBuf longValue = encoder.encodeLongBinary(value, encoders);
            longValues.add(new SendLongDataMessage(psInfo.statementId, paramIndex, longValue));
        } else {
            encoder.encodeBinary(value, valueBytes, encoders);
        }
    }

    private Result sendDataAfterReset() {
        state = STATE_AWAITING_COLUMN_COUNT;

        for (SendLongDataMessage msg : longValues) {
            psInfo.markLongParamsSent();
            conn.sendMessage(msg);
        }

        conn.sendMessage(execMessage);

        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        switch (state) {
            case STATE_AWAITING_RESET_RESULT:
                return processPacket_AWAITING_RESET_RESULT(packet);

            case STATE_AWAITING_COLUMN_COUNT:
                return processPacket_AWAITING_COLUMN_COUNT(packet);

            case STATE_READING_COLUMNS:
                return processPacket_READING_COLUMNS(packet);

            case STATE_READING_ROWS:
                return processPacket_READING_ROWS(packet);

            default:
                return Result.protocolErrorAbortEverything("Unexpected state (" + state + ")");
        }
    }

    private Result processPacket_AWAITING_RESET_RESULT(ByteBuf packet) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_OK:
                return sendDataAfterReset();

            case MyConstants.PACKET_HEADER_ERR:
                FutureUtils.failWithError(promise, conn.decodeErrorAfterHeader(packet));
                return Result.stateMachineFinished();

            default:
                return Result.unknownHeaderByte(header, "execute prepared statement (after reset)");
        }
    }

    private Result processPacket_AWAITING_COLUMN_COUNT(ByteBuf packet) {
        int header = consumePacketHeader(packet);

        switch (header) {
            case NO_PACKET_HEADER:
                return Result.unknownHeaderByte(header, "execute prepared statement (awaiting column count)");

            case PACKET_HEADER_OK:
                // looks like we have no rows..
                OkMessage okMsg = OkMessage.decodeAfterHeader(packet, UTF_8);
                QR queryResult = rsFactory.makeQueryResultWithNoRows(okMsg.affectedRows, okMsg.message, okMsg.lastInsertId, okMsg.statusFlags, okMsg.warnings);
                promise.complete(queryResult);
                return Result.stateMachineFinished();

            case PACKET_HEADER_ERR:
                FutureUtils.failWithError(promise, conn.decodeErrorAfterHeader(packet));
                return Result.stateMachineFinished();
        }

        long numColumns = ByteBufUtils.readBinaryLength(header, packet);
        if (numColumns < 1 || numColumns > Integer.MAX_VALUE)
            return Result.protocolErrorAbortEverything("Invalid number of columns received");

        state = STATE_READING_COLUMNS;
        return Result.expectingMorePackets();
    }

    private Result processPacket_READING_COLUMNS(ByteBuf packet) {
        if (isEOFPacket(packet)) {
            state = STATE_READING_ROWS;
            resultSetBuilder = rsFactory.makeResultSetBuilder(encoders, MyDbColumns.create(columnDefs));
            return Result.expectingMorePackets();
        }

        ColumnDefinitionMessage colDef = ColumnDefinitionMessage.decode(columnDefs.size(), packet, UTF_8);
        columnDefs.add(colDef);
        return Result.expectingMorePackets();
    }

    private Result processPacket_READING_ROWS(ByteBuf packet) {
        if (isEOFPacket(packet)) {
            EOFMessage eof = EOFMessage.decode(packet);
            QR queryResult = resultSetBuilder.build(eof.flags, eof.warningCount);
            promise.complete(queryResult);
            return Result.stateMachineFinished();
        }

        if (isERRPacket(packet)) {
            packet.readByte();
            FutureUtils.failWithError(promise, conn.decodeErrorAfterHeader(packet));
            return Result.stateMachineFinished();
        }

        return decodeRow(packet);
    }

    private Result decodeRow(ByteBuf buffer) {
        buffer.readByte(); // header

        int numColumns = columnDefs.size();
        int nullCount = (numColumns + 9) / 8;

        byte[] nullBytes = new byte[nullCount];
        buffer.readBytes(nullBytes);

        try {
            resultSetBuilder.startRow();
            for (int i = 0; i < numColumns; i++) {
                MyDbColumn column = columnDefs.get(i);

                if (ByteBufUtils.isNullBitSet(2, nullBytes, i)) {
                    resultSetBuilder.nullValue(column);
                } else {
                    resultSetBuilder.binaryValue(column, buffer);
                }
            }
            resultSetBuilder.endRow();
        } catch (Throwable t) {
            // protect against broken result set builders?
            return Result.protocolErrorAbortEverything(t);
        }

        if (buffer.readableBytes() != 0) {
            return Result.protocolErrorAbortEverything(new BufferNotFullyConsumedException(buffer));
        } else {
            return Result.expectingMorePackets();
        }
    }
}
