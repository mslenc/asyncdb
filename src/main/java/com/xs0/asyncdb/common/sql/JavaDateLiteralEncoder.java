package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

public class JavaDateLiteralEncoder implements SqlLiteralEncoder {
    private static final JavaDateLiteralEncoder instance = new JavaDateLiteralEncoder();

    public static JavaDateLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Date date = (Date) value;
        LocalDateTime localDateTime = date.toInstant().atZone(settings.timezone()).toLocalDateTime();
        LocalDateTimeLiteralEncoder.instance().encode(localDateTime, out, settings);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Date.class);
    }
}
