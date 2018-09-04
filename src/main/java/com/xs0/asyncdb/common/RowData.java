package com.xs0.asyncdb.common;

import java.util.List;

public interface RowData {
    Object get(int columnIndex);
    Object get(String columnName);

    int getRowNumber();
}
