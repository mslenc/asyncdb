package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;

public class InstantLiteralEncoder implements SqlLiteralEncoder {
    private static final InstantLiteralEncoder instance = new InstantLiteralEncoder();

    public static InstantLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Instant instant = (Instant) value;
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, settings.serverTimezone());
        LocalDateTimeLiteralEncoder.instance().encode(dateTime.toLocalDateTime(), out, settings);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Instant.class);
    }
}
