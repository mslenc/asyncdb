package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.DbResultSet;
import com.github.mslenc.asyncdb.DbRow;

import java.util.AbstractList;
import java.util.List;

public class JdbcResultSet extends AbstractList<DbRow> implements DbResultSet {
    private final JdbcColumns columns;
    private final List<DbRow> rows;

    public JdbcResultSet(JdbcColumns columns, List<DbRow> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    @Override
    public JdbcColumns getColumns() {
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
