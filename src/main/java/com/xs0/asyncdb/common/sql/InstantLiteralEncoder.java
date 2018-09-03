package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

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
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, settings.timezone());
        LocalDateTimeLiteralEncoder.instance().encode(dateTime.toLocalDateTime(), out, settings);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Instant.class);
    }
}
