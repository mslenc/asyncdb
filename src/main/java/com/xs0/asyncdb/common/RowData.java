package com.xs0.asyncdb.common;

import java.util.List;

public interface RowData extends List<Object> {
    Object get(String columnName);
    int getRowNumber();
}
