package com.github.mslenc.asyncdb.mysql.column;

import java.time.LocalDate;

public class LocalDateTextDecoder implements TextValueDecoder {
    private static final LocalDateTextDecoder instance = new LocalDateTextDecoder();

    public static LocalDateTextDecoder instance() {
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
