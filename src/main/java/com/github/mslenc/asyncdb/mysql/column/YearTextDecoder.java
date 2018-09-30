package com.github.mslenc.asyncdb.mysql.column;

import java.time.Year;

public class YearTextDecoder implements TextValueDecoder {
    private static final YearTextDecoder instance = new YearTextDecoder();

    public static YearTextDecoder instance() {
        return instance;
    }

    @Override
    public Year decode(String value) {
        return Year.of(Integer.parseInt(value));
    }
}