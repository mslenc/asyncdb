package com.xs0.asyncdb.common.column;

import java.time.Year;

public class YearEncoderDecoder implements ColumnEncoderDecoder {
    private static final YearEncoderDecoder instance = new YearEncoderDecoder();

    public static YearEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Year decode(String value) {
        return Year.of(Integer.parseInt(value));
    }
}