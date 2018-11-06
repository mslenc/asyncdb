package com.github.mslenc.asyncdb.impl;

import com.github.mslenc.asyncdb.DbColumn;
import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.DbRow;
import com.github.mslenc.asyncdb.DbValue;

import java.util.List;

public class DbRowImpl implements DbRow {
    private final int rowIndex;
    private DbValue[] values;
    private DbColumns columns;

    public DbRowImpl(int rowIndex, DbValue[] values, DbColumns columns) {
        this.rowIndex = rowIndex;
        this.values = values;
        this.columns = columns;
    }

    public static DbRowImpl copyFrom(List<DbValue> values, DbColumns columns, int rowIndex) {
        DbValue[] copy = values.toArray(new DbValue[0]);
        return new DbRowImpl(rowIndex, copy, columns);
    }

    public DbColumns getColumns() {
        return columns;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public DbValue getValue(int columnIndex) {
        return values[columnIndex];
    }

    @Override
    public DbValue getValue(String columnName) {
        DbColumn column = columns.get(columnName);
        if (column == null)
            throw new IllegalArgumentException("No column named " + columnName);

        return values[column.getIndexInRow()];
    }
}
