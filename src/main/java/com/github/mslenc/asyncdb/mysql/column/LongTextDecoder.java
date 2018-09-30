package com.github.mslenc.asyncdb.mysql.column;

public class LongTextDecoder implements TextValueDecoder {
    private static final LongTextDecoder instance = new LongTextDecoder();

    public static LongTextDecoder instance() {
        return instance;
    }

    @Override
    public Long decode(String value) {
        return Long.valueOf(value);
    }
}