package com.xs0.asyncdb.common;

public interface RowData {
    Object get(int columnIndex);
    Object get(String columnName);

    int getRowNumber();
}
