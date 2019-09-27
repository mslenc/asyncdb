package com.github.mslenc.asyncdb.util;

import com.github.mslenc.asyncdb.DbColumn;
import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.DbResultSet;
import com.github.mslenc.asyncdb.DbRow;

import java.util.AbstractList;

class EmptyColumns extends AbstractList<DbColumn> implements DbColumns {
    static final EmptyColumns INSTANCE = new EmptyColumns();

    @Override
    public DbColumn get(String columnName) {
        return null;
    }

    @Override
    public DbColumn get(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return 0;
    }
}

public class EmptyResultSet extends AbstractList<DbRow> implements DbResultSet {
    public static final EmptyResultSet INSTANCE = new EmptyResultSet();

    @Override
    public DbColumns getColumns() {
        return EmptyColumns.INSTANCE;
    }

    @Override
    public DbRow get(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return 0;
    }
}
