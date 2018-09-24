package com.github.mslenc.asyncdb.common.column;

interface ColumnEncoderRegistry {
    String encode(Object value);
    int kindOf(Object value);
}
