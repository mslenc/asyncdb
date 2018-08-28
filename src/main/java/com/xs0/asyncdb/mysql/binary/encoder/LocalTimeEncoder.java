package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.LocalTime;

public class LocalTimeEncoder implements BinaryEncoder {
    @Override
    public void encode(Object value, ByteBuf buffer) {
        LocalTime time = (LocalTime) value;

        int micros = time.getNano() / 1000;
        boolean hasMicros = micros != 0;

        buffer.writeByte(hasMicros ? 12 : 8);
        buffer.writeByte(0); // we can't have negative values..
        buffer.writeInt(0); // days
        buffer.writeByte(time.getHour());
        buffer.writeByte(time.getMinute());
        buffer.writeByte(time.getSecond());
        if (hasMicros) {
            buffer.writeInt(micros);
        }
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_TIME;
    }
}
