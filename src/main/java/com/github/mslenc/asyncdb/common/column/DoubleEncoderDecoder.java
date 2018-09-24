package com.github.mslenc.asyncdb.common.column;

public class DoubleEncoderDecoder implements ColumnEncoderDecoder {
    private static final DoubleEncoderDecoder instance = new DoubleEncoderDecoder();

    public static DoubleEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Double decode(String value) {
        return Double.valueOf(value);
    }
}
