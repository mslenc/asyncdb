package com.github.mslenc.asyncdb.common.column;

import com.github.mslenc.asyncdb.common.ULong;

public class UnsignedLongEncoderDecoder implements ColumnEncoderDecoder {
    private static final UnsignedLongEncoderDecoder instance = new UnsignedLongEncoderDecoder();

    public static UnsignedLongEncoderDecoder instance() {
        return instance;
    }

    @Override
    public ULong decode(String value) {
        return ULong.valueOf(value);
    }
}