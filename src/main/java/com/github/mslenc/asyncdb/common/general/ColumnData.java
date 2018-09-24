package com.github.mslenc.asyncdb.common.general;

public interface ColumnData {
    String name();
    int dataType();
    long dataTypeSize();
    boolean isUnsigned();
}
