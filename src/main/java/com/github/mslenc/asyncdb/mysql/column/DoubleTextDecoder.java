package com.github.mslenc.asyncdb.mysql.column;

public class DoubleTextDecoder implements TextValueDecoder {
    private static final DoubleTextDecoder instance = new DoubleTextDecoder();

    public static DoubleTextDecoder instance() {
        return instance;
    }

    @Override
    public Double decode(String value) {
        return Double.valueOf(value);
    }
}
