package com.xs0.asyncdb.common.column;

public class ShortEncoderDecoder implements ColumnEncoderDecoder {
    private static final ShortEncoderDecoder instance = new ShortEncoderDecoder();

    public static ShortEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Short decode(String value) {
        return Short.valueOf(value);
    }
}