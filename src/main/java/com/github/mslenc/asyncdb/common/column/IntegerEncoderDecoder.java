package com.github.mslenc.asyncdb.common.column;

public class IntegerEncoderDecoder implements ColumnEncoderDecoder {
    private static final IntegerEncoderDecoder instance = new IntegerEncoderDecoder();

    public static IntegerEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Integer decode(String value) {
        return Integer.valueOf(value);
    }
}
