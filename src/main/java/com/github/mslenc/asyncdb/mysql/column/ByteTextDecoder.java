package com.github.mslenc.asyncdb.mysql.column;

public class ByteTextDecoder implements TextValueDecoder {
    private static final ByteTextDecoder instance = new ByteTextDecoder();

    public static ByteTextDecoder instance() {
        return instance;
    }

    @Override
    public Byte decode(String value) {
        return Byte.valueOf(value);
    }
}
