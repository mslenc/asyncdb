package com.xs0.asyncdb.common.column;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TimeEncoderDecoder implements ColumnEncoderDecoder {
    private static final TimeEncoderDecoder instance = new TimeEncoderDecoder();

    public static TimeEncoderDecoder instance() {
        return instance;
    }

    private DateTimeFormatter optional =
        new DateTimeFormatterBuilder().
            appendPattern(".SSSSSS").
            toFormatter();

    private DateTimeFormatter format =
        new DateTimeFormatterBuilder().
            appendPattern("HH:mm:ss").
            appendOptional(optional).
            toFormatter();

    private DateTimeFormatter printer =
        new DateTimeFormatterBuilder().
            appendPattern("HH:mm:ss.SSSSSS").
            toFormatter();

    protected DateTimeFormatter parseFormat() {
        return format;
    }

    @Override
    public LocalTime decode(String value) {
        return LocalTime.parse(value, parseFormat());
    }

    @Override
    public String encode(Object value) {
        return printer.format((LocalTime)value);
    }
}
