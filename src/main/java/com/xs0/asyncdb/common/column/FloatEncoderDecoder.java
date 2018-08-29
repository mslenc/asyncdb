package com.xs0.asyncdb.common.column;

public class FloatEncoderDecoder implements ColumnEncoderDecoder {
    private static final FloatEncoderDecoder instance = new FloatEncoderDecoder();

    public static FloatEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Float decode(String value) {
        return Float.valueOf(value);
    }
}
