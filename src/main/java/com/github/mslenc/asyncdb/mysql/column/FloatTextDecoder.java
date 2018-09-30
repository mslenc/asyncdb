package com.github.mslenc.asyncdb.mysql.column;

public class FloatTextDecoder implements TextValueDecoder {
    private static final FloatTextDecoder instance = new FloatTextDecoder();

    public static FloatTextDecoder instance() {
        return instance;
    }

    @Override
    public Float decode(String value) {
        return Float.valueOf(value);
    }
}
