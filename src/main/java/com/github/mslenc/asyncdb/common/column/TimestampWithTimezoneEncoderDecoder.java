package com.github.mslenc.asyncdb.common.column;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampWithTimezoneEncoderDecoder extends TimestampEncoderDecoder {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSZ");
    private static final TimestampWithTimezoneEncoderDecoder instance = new TimestampWithTimezoneEncoderDecoder();

    public static TimestampWithTimezoneEncoderDecoder instance() {
        return instance;
    }

    @Override
    protected DateTimeFormatter parseFormat() {
        return format;
    }

    @Override
    public LocalDateTime decode(String value) {
        return LocalDateTime.parse(value, format);
    }
}