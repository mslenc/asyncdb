package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;

public class LocalDateTimeEncoder implements BinaryEncoder {
    private static final LocalDateTimeEncoder instance = new LocalDateTimeEncoder();

    public static LocalDateTimeEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        LocalDateTime dateTime = (LocalDateTime) value;

        int micros = dateTime.getNano() / 1000;
        boolean hasMicros = micros != 0;

        buffer.writeByte(hasMicros ? 11 : 7);

        buffer.writeShortLE(dateTime.getYear());
        buffer.writeByte(dateTime.getMonthValue());
        buffer.writeByte(dateTime.getDayOfMonth());
        buffer.writeByte(dateTime.getHour());
        buffer.writeByte(dateTime.getMinute());
        buffer.writeByte(dateTime.getSecond());

        if (hasMicros) {
            buffer.writeIntLE(micros);
        }
    }

    @Override
    public int encodesTo() {
        return MySQLIO.FIELD_TYPE_TIMESTAMP;
    }
}
