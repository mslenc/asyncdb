package com.xs0.asyncdb.common.column;

interface ColumnEncoderRegistry {
    String encode(Object value);
    int kindOf(Object value);
}
