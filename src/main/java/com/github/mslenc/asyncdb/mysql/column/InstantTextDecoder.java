package com.github.mslenc.asyncdb.mysql.column;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class InstantTextDecoder implements TextValueDecoder {
    private static final InstantTextDecoder instance = new InstantTextDecoder();

    public static InstantTextDecoder instance() {
        return instance;
    }

    @Override
    public Instant decode(String value) {
        LocalDateTime ldt = LocalDateTimeTextDecoder.instance().decode(value);
        if (ldt == null)
            return null;

        return ldt.atZone(ZoneOffset.UTC).toInstant();
    }
}
