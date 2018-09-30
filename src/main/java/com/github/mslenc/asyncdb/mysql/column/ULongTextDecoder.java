package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.ULong;

public class ULongTextDecoder implements TextValueDecoder {
    private static final ULongTextDecoder instance = new ULongTextDecoder();

    public static ULongTextDecoder instance() {
        return instance;
    }

    @Override
    public ULong decode(String value) {
        return ULong.valueOf(value);
    }
}