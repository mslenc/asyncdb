package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.LocalTime;

public class LocalTimeEncoder implements BinaryEncoder {
    private static final LocalTimeEncoder instance = new LocalTimeEncoder();

    public static LocalTimeEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        LocalTime time = (LocalTime) value;

        int micros = time.getNano() / 1000;
        boolean hasMicros = micros != 0;

        buffer.writeByte(hasMicros ? 12 : 8);
        buffer.writeByte(0); // we can't have negative values..
        buffer.writeIntLE(0); // days
        buffer.writeByte(time.getHour());
        buffer.writeByte(time.getMinute());
        buffer.writeByte(time.getSecond());
        if (hasMicros) {
            buffer.writeIntLE(micros);
        }
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TIME;
    }
}
