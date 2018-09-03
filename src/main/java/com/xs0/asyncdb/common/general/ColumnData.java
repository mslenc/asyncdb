package com.xs0.asyncdb.common.general;

public interface ColumnData {
    String name();
    int dataType();
    long dataTypeSize();
    boolean isUnsigned();
}
