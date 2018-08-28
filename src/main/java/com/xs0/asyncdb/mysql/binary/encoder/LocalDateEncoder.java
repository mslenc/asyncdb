package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.LocalDate;


public class LocalDateEncoder implements BinaryEncoder {
    @Override
    public void encode(Object value, ByteBuf buffer) {
        LocalDate date = (LocalDate) value;

        buffer.writeByte(4);
        buffer.writeShort(date.getYear());
        buffer.writeByte(date.getMonthValue());
        buffer.writeByte(date.getDayOfMonth());
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_DATE;
    }
}
