package com.github.mslenc.asyncdb.common.column;

public class StringEncoderDecoder implements ColumnDecoder {
    private static final StringEncoderDecoder instance = new StringEncoderDecoder();

    public static StringEncoderDecoder instance() {
        return instance;
    }

    @Override
    public String decode(String value) {
        return value;
    }
}
