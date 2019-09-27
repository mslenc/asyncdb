package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.impl.DbQueryResultImpl;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilder;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilderFactory;
import com.github.mslenc.asyncdb.util.EmptyResultSet;
import com.github.mslenc.asyncdb.util.GeneratedIdResult;

class MyDbQueryResultBuilderFactory implements MyResultSetBuilderFactory<DbExecResult> {
    static final MyDbQueryResultBuilderFactory instance = new MyDbQueryResultBuilderFactory();

    @Override
    public MyResultSetBuilder<DbExecResult> makeResultSetBuilder(MyEncoders encoders, MyDbColumns columns) {
        return new MyDbQueryResultBuilder(encoders, columns);
    }

    @Override
    public DbExecResult makeQueryResultWithNoRows(long rowsAffected, String message, long lastInsertId, int statusFlags, int warnings) {
        DbResultSet ids = lastInsertId > 0 ? new GeneratedIdResult(lastInsertId) : EmptyResultSet.INSTANCE;
        return new DbQueryResultImpl(rowsAffected, message, null, ids);
    }
}