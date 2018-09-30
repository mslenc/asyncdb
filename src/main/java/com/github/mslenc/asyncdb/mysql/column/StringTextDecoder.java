package com.github.mslenc.asyncdb.mysql.column;

public class StringTextDecoder implements TextValueDecoder {
    private static final StringTextDecoder instance = new StringTextDecoder();

    public static StringTextDecoder instance() {
        return instance;
    }

    @Override
    public String decode(String value) {
        return value;
    }
}
