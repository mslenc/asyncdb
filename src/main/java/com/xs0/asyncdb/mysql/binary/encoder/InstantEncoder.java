package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
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
    public void encode(Object value, ByteBuf buffer) {
        Instant instant = (Instant) value;
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()); // TODO: timezone handling
        LocalDateTimeEncoder.instance().encode(ldt, buffer);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_TIMESTAMP;
    }
}
