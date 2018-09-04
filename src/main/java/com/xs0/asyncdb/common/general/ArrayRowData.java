package com.xs0.asyncdb.common.general;

import com.xs0.asyncdb.common.RowData;

import java.util.Map;

public class ArrayRowData implements RowData {
    private final int rowNumber;
    private final Map<String, Integer> mapping;
    private final Object[] values;

    public ArrayRowData(int rowNumber, Map<String, Integer> mapping, Object[] values) {
        this.rowNumber = rowNumber;
        this.mapping = mapping;
        this.values = values;
    }

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public Object get(int index) {
        return values[index];
    }

    @Override
    public Object get(String columnName) {
        return values[mapping.get(columnName)];
    }
}
