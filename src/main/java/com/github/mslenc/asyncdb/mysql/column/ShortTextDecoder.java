package com.github.mslenc.asyncdb.mysql.column;

public class ShortTextDecoder implements TextValueDecoder {
    private static final ShortTextDecoder instance = new ShortTextDecoder();

    public static ShortTextDecoder instance() {
        return instance;
    }

    @Override
    public Short decode(String value) {
        return Short.valueOf(value);
    }
}