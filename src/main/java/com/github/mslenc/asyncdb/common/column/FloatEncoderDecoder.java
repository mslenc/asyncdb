package com.github.mslenc.asyncdb.common.column;

public class FloatEncoderDecoder implements ColumnDecoder {
    private static final FloatEncoderDecoder instance = new FloatEncoderDecoder();

    public static FloatEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Float decode(String value) {
        return Float.valueOf(value);
    }
}
