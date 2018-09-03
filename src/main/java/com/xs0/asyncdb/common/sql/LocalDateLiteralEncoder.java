package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.Set;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

public class LocalDateLiteralEncoder implements SqlLiteralEncoder {
    private static final LocalDateLiteralEncoder instance = new LocalDateLiteralEncoder();

    public static LocalDateLiteralEncoder instance() {
        return instance;
    }

    private static DateTimeFormatter formatter =
            new DateTimeFormatterBuilder()
                    .appendValue(YEAR, 4)
                    .appendLiteral('-')
                    .appendValue(MONTH_OF_YEAR, 2)
                    .appendLiteral('-')
                    .appendValue(DAY_OF_MONTH, 2)
                    .toFormatter();

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        LocalDate date = (LocalDate) value;
        out.append(date.format(formatter));
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(LocalDate.class);
    }
}
