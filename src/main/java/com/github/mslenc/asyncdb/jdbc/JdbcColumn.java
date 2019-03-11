package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.DbColumn;
import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.jdbc.val.JdbcGetter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcColumn implements DbColumn {
    private final String name;
    private final int indexInRow;
    private final JdbcGetter getter;

    public JdbcColumn(String name, int indexInRow, JdbcGetter getter) {
        this.name = name;
        this.indexInRow = indexInRow;
        this.getter = getter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getIndexInRow() {
        return indexInRow;
    }

    public DbValue extract(ResultSet rs) throws SQLException {
        return getter.get(rs, indexInRow + 1);
    }
}
