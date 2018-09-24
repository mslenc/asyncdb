package com.github.mslenc.asyncdb.common.column;

import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

import static java.time.temporal.ChronoField.*;

public class SQLTimeEncoder implements ColumnEncoder {
    private static final SQLTimeEncoder instance = new SQLTimeEncoder();

    public static SQLTimeEncoder instance() {
        return instance;
    }

    private static final DateTimeFormatter format =
        new DateTimeFormatterBuilder().
            appendValue(HOUR_OF_DAY, 2).
            appendLiteral(':').
            appendValue(MINUTE_OF_HOUR, 2).
            appendLiteral(':').
            appendValue(SECOND_OF_MINUTE, 2).
            optionalStart().
            appendFraction(NANO_OF_SECOND, 0, 9, true).
            toFormatter().
            withResolverStyle(ResolverStyle.STRICT);

    @Override
    public String encode(Object value) {
        return format.format(((Time) value).toLocalTime());
    }
}
