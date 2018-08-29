package com.xs0.asyncdb.common.column;

public class ByteDecoder implements ColumnDecoder {
    private static final ByteDecoder instance = new ByteDecoder();

    public static ByteDecoder instance() {
        return instance;
    }

    @Override
    public Byte decode(String value) {
        return Byte.valueOf(value);
    }
}
