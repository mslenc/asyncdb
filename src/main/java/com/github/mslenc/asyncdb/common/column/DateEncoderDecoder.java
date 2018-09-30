package com.github.mslenc.asyncdb.common.column;

import java.time.LocalDate;

public class DateEncoderDecoder implements ColumnDecoder {
    private static final DateEncoderDecoder instance = new DateEncoderDecoder();

    public static DateEncoderDecoder instance() {
        return instance;
    }

    private static final String ZeroedDate = "0000-00-00";

    @Override
    public LocalDate decode(String value) {
        if (ZeroedDate.equals(value)) {
            return null;
        } else {
            return LocalDate.parse(value);
        }
    }
}
