package com.xs0.asyncdb.common.column;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalDateTimeEncoderDecoder implements ColumnEncoderDecoder {
    private static final LocalDateTimeEncoderDecoder instance = new LocalDateTimeEncoderDecoder();

    public static LocalDateTimeEncoderDecoder instance() {
        return instance;
    }

    private static final String ZeroedTimestamp = "0000-00-00 00:00:00";

    private DateTimeFormatter optional =
        new DateTimeFormatterBuilder().
            appendPattern(".SSSSSS").
            toFormatter();

    private DateTimeFormatter format =
        new DateTimeFormatterBuilder().
            appendPattern("yyyy-MM-dd HH:mm:ss").
            appendOptional(optional).
            toFormatter();

    @Override
    public String encode(Object value) {
        return format.format((LocalDateTime)value);
    }

    @Override
    public Object decode(String value) {
        if (value.equals(ZeroedTimestamp)) {
            return null;
        } else {
            return LocalDateTime.parse(value, format);
        }
    }
}
