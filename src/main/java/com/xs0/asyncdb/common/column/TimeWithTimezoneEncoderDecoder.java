package com.xs0.asyncdb.common.column;

import java.time.format.DateTimeFormatter;

public class TimeWithTimezoneEncoderDecoder extends TimeEncoderDecoder {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSZ");

    @Override
    protected DateTimeFormatter parseFormat() {
        return format;
    }
}
