package com.github.mslenc.asyncdb.impl;

import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.DbResultSet;
import com.github.mslenc.asyncdb.DbRow;

import java.util.AbstractList;
import java.util.List;

public class DbResultSetImpl extends AbstractList<DbRow> implements DbResultSet {
    private final DbColumns columns;
    private final List<DbRow> rows;

    public DbResultSetImpl(DbColumns columns, List<DbRow> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    @Override
    public DbColumns getColumns() {
        return columns;
    }

    @Override
    public DbRow get(int index) {
        return rows.get(index);
    }

    @Override
    public int size() {
        return rows.size();
    }
}
