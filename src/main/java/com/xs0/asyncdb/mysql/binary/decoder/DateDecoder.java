package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateDecoder implements BinaryDecoder {
    private static final DateDecoder instance = new DateDecoder();

    public static DateDecoder instance() {
        return instance;
    }

    @Override
    public LocalDate decode(ByteBuf buffer) {
        LocalDateTime ts = TimestampDecoder.instance().decode(buffer);

        if (ts != null) {
            return ts.toLocalDate();
        } else {
            return null;
        }
    }
}
