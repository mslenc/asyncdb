package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;

public class CalendarLiteralEncoder implements SqlLiteralEncoder {
    private static final CalendarLiteralEncoder instance = new CalendarLiteralEncoder();

    public static CalendarLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Calendar calendar = (Calendar) value;

        long millis = calendar.getTimeInMillis();
        long seconds = millis / 1000;
        int nanos = (int)(millis % 1000) * 1000000;
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(calendar.getTimeZone().getRawOffset() / 1000);
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(seconds, nanos, zoneOffset);

        LocalDateTimeLiteralEncoder.instance().encode(localDateTime, out, settings);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Calendar.class);
    }
}
