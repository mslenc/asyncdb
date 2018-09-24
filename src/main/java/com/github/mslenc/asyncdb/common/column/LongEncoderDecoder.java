package com.github.mslenc.asyncdb.common.column;

public class LongEncoderDecoder implements ColumnEncoderDecoder {
    private static final LongEncoderDecoder instance = new LongEncoderDecoder();

    public static LongEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Long decode(String value) {
        return Long.valueOf(value);
    }
}