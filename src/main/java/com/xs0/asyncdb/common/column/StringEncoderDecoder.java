package com.xs0.asyncdb.common.column;

public class StringEncoderDecoder implements ColumnEncoderDecoder {
    private static final StringEncoderDecoder instance = new StringEncoderDecoder();

    public static StringEncoderDecoder instance() {
        return instance;
    }

    @Override
    public String decode(String value) {
        return value;
    }
}
