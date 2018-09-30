package com.github.mslenc.asyncdb.mysql.column;

public class IntegerTextDecoder implements TextValueDecoder {
    private static final IntegerTextDecoder instance = new IntegerTextDecoder();

    public static IntegerTextDecoder instance() {
        return instance;
    }

    @Override
    public Integer decode(String value) {
        return Integer.valueOf(value);
    }
}
