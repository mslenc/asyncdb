package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.Set;

import static java.time.temporal.ChronoField.*;

public class LocalDateTimeLiteralEncoder implements SqlLiteralEncoder {
    private static final LocalDateTimeLiteralEncoder instance = new LocalDateTimeLiteralEncoder();

    public static LocalDateTimeLiteralEncoder instance() {
        return instance;
    }

    private static DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral(' ')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendLiteral('.')
            .appendValue(MICRO_OF_SECOND, 6)
            .toFormatter();

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        LocalDateTime dateTime = (LocalDateTime) value;
        out.append("'");
        out.append(dateTime.format(formatter));
        out.append("'");
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(LocalDateTime.class);
    }
}
