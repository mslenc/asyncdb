package com.github.mslenc.asyncdb.common.column;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class InstantEncoderDecoder implements ColumnEncoderDecoder {
    private static final InstantEncoderDecoder instance = new InstantEncoderDecoder();

    public static InstantEncoderDecoder instance() {
        return instance;
    }

    @Override
    public String encode(Object value) {
        Instant instant = (Instant) value;
        LocalDateTime ldt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDateTime();
        return LocalDateTimeEncoderDecoder.instance().encode(ldt);
    }

    @Override
    public Object decode(String value) {
        LocalDateTime ldt = LocalDateTimeEncoderDecoder.instance().decode(value);
        if (ldt == null)
            return null;

        return ldt.atZone(ZoneOffset.UTC).toInstant();
    }
}
