package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import java.time.LocalDate;

public class LocalDateEncoder implements BinaryEncoder {
    private static final LocalDateEncoder instance = new LocalDateEncoder();

    public static LocalDateEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        LocalDate date = (LocalDate) value;

        buffer.writeByte(4);
        buffer.writeShortLE(date.getYear());
        buffer.writeByte(date.getMonthValue());
        buffer.writeByte(date.getDayOfMonth());
    }

    @Override
    public int encodesTo() {
        return MySQLIO.FIELD_TYPE_DATE;
    }
}
