package com.github.mslenc.asyncdb.common.column;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class InstantEncoderDecoder implements ColumnDecoder {
    private static final InstantEncoderDecoder instance = new InstantEncoderDecoder();

    public static InstantEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Instant decode(String value) {
        LocalDateTime ldt = LocalDateTimeEncoderDecoder.instance().decode(value);
        if (ldt == null)
            return null;

        return ldt.atZone(ZoneOffset.UTC).toInstant();
    }
}
