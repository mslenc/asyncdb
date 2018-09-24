package com.github.mslenc.asyncdb.common;

public interface RowData {
    Object get(int columnIndex);
    Object get(String columnName);

    int getRowNumber();
}
