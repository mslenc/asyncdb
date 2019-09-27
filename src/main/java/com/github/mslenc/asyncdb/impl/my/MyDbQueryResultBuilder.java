package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.DbExecResult;
import com.github.mslenc.asyncdb.DbRow;
import com.github.mslenc.asyncdb.impl.DbColumnsImpl;
import com.github.mslenc.asyncdb.impl.DbQueryResultImpl;
import com.github.mslenc.asyncdb.impl.DbResultSetImpl;
import com.github.mslenc.asyncdb.impl.DbRowImpl;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.my.resultset.MyDbRowResultSetBuilder;
import com.github.mslenc.asyncdb.util.EmptyResultSet;

import java.util.ArrayList;

class MyDbQueryResultBuilder extends MyDbRowResultSetBuilder<DbExecResult> {
    private ArrayList<DbRow> rows = new ArrayList<>();

    public MyDbQueryResultBuilder(MyEncoders encoders, MyDbColumns columns) {
        super(encoders, columns);
    }

    @Override
    protected void onRow(DbRowImpl row) {
        rows.add(row);
    }

    @Override
    public DbExecResult build(int statusFlags, int warnings) {
        DbResultSetImpl queryResult = new DbResultSetImpl(new DbColumnsImpl(columns()), rows);
        return new DbQueryResultImpl(0, null, queryResult, EmptyResultSet.INSTANCE);
    }
}