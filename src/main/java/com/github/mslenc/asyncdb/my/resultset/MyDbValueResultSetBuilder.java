package com.github.mslenc.asyncdb.my.resultset;

import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.util.ULong;
import com.github.mslenc.asyncdb.my.MyDbColumn;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.impl.values.*;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.time.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class MyDbValueResultSetBuilder<QR> extends MyDecodingResultSetBuilder<QR> {
    protected abstract void onDbValue(MyDbColumn column, DbValue dbValue);
    
    private final MyEncoders encoders;
    private final MyDbColumns columns;
    private final DbColumns dbColumns;

    protected MyDbValueResultSetBuilder(MyEncoders encoders, MyDbColumns columns) {
        this.encoders = encoders;
        this.columns = columns;
        this.dbColumns = columns.toDbColumns();
    }

    protected MyEncoders encoders() {
        return encoders;
    }

    protected MyDbColumns columns() {
        return columns;
    }

    protected DbColumns dbColumns() {
        return dbColumns;
    }

    @Override
    public void nullValue(MyDbColumn column) {
        onDbValue(column, DbValueNull.instance());
    }

    @Override
    protected void onBlobValue(MyDbColumn column, ByteBuf buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        onDbValue(column, new DbValueByteArray(bytes));
    }

    @Override
    protected void onTextValue(MyDbColumn column, String string) {
        onDbValue(column, new DbValueString(string));
    }

    @Override
    protected void onTimeValue(MyDbColumn column, boolean negative, int hours, int minutes, int seconds, int micros) {
        long nanos = hours * (60 * 60 * 1_000_000_000L) +
                     minutes * (   60 * 1_000_000_000L) +
                     seconds * (        1_000_000_000L) +
                     micros * (                 1_000L);

        onDbValue(column, new DbValueDuration(Duration.ofNanos(negative ? -nanos : nanos)));
    }

    @Override
    protected void onGeometryValue(MyDbColumn column, ByteBuf buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        onDbValue(column, new DbValueByteArray(bytes));
    }

    @Override
    protected void onDateValue(MyDbColumn column, int year, int month, int day) {
        onDbValue(column, new DbValueLocalDate(LocalDate.of(year, month, day)));
    }

    @Override
    protected void onTimestampValue(MyDbColumn column, int year, int month, int day, int hour, int minute, int second, int micro) {
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second, micro * 1000);

        Instant timestamp = dateTime.toInstant(ZoneOffset.UTC);
        onDbValue(column, new DbValueInstant(timestamp, dateTime));
    }

    @Override
    protected void onDateTimeValue(MyDbColumn column, int year, int month, int day, int hour, int minute, int second, int micro) {
        onDbValue(column, new DbValueLocalDateTime(LocalDateTime.of(year, month, day, hour, minute, second, 1000 * micro)));
    }

    @Override
    protected void onDecimalValue(MyDbColumn column, String valueStr) {
        onDbValue(column, new DbValueBigDecimal(new BigDecimal(valueStr)));
    }

    @Override
    protected void onYearValue(MyDbColumn column, int value) {
        onDbValue(column, new DbValueYear(Year.of(value)));
    }

    @Override
    protected void onFloatValue(MyDbColumn column, float value) {
        onDbValue(column, new DbValueFloat(value));
    }

    @Override
    protected void onDoubleValue(MyDbColumn column, double value) {
        onDbValue(column, new DbValueDouble(value));
    }

    @Override
    protected void onTinyValue(MyDbColumn column, byte value) {
        onDbValue(column, new DbValueInt(value));
    }

    @Override
    protected void onUnsignedTinyValue(MyDbColumn column, short value) {
        onDbValue(column, new DbValueInt(value));
    }

    @Override
    protected void onShortValue(MyDbColumn column, short value) {
        onDbValue(column, new DbValueInt(value));
    }

    @Override
    protected void onUnsignedShortValue(MyDbColumn column, int value) {
        onDbValue(column, new DbValueInt(value));
    }

    @Override
    protected void onInt24Value(MyDbColumn column, int value) {
        onDbValue(column, new DbValueInt(value));
    }

    @Override
    protected void onUnsignedInt24Value(MyDbColumn column, int value) {
        onDbValue(column, new DbValueInt(value));
    }

    @Override
    protected void onLongValue(MyDbColumn column, int value) {
        onDbValue(column, new DbValueInt(value));
    }

    @Override
    protected void onUnsignedLongValue(MyDbColumn column, long value) {
        onDbValue(column, new DbValueLong(value));
    }

    @Override
    protected void onLongLongValue(MyDbColumn column, long value) {
        onDbValue(column, new DbValueLong(value));
    }

    @Override
    protected void onUnsignedLongLongValue(MyDbColumn column, long value) {
        onDbValue(column, new DbValueULong(ULong.valueOf(value)));
    }

    @Override
    protected void onJsonValue(MyDbColumn column, ByteBuf buffer, int length) {
        String json = ByteBufUtils.readFixedString(buffer, length, UTF_8);
        onDbValue(column, new DbValueString(json));
    }
}
