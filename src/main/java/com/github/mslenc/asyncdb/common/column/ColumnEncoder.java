package com.github.mslenc.asyncdb.common.column;

public interface ColumnEncoder {
    default String encode(Object value) {
        return value.toString();
    }
}
