package com.github.mslenc.asyncdb.common.column;

import java.time.Year;

public class YearEncoderDecoder implements ColumnDecoder {
    private static final YearEncoderDecoder instance = new YearEncoderDecoder();

    public static YearEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Year decode(String value) {
        return Year.of(Integer.parseInt(value));
    }
}