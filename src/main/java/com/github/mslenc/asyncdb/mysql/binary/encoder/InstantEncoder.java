package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class InstantEncoder implements BinaryEncoder {
    private static final InstantEncoder instance = new InstantEncoder();

    public static InstantEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        Instant instant = (Instant) value;
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, codecSettings.serverTimezone());
        LocalDateTimeEncoder.instance().encode(ldt, buffer, codecSettings);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TIMESTAMP;
    }
}
