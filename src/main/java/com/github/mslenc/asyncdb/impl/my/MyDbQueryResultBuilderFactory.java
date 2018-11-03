package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.impl.DbQueryResultImpl;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilder;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilderFactory;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;


class MyDbQueryResultBuilderFactory implements MyResultSetBuilderFactory<DbExecResult> {
    static final MyDbQueryResultBuilderFactory instance = new MyDbQueryResultBuilderFactory();

    @Override
    public MyResultSetBuilder<DbExecResult> makeResultSetBuilder(MyEncoders encoders, MyDbColumns columns) {
        return new MyDbQueryResultBuilder(encoders, columns);
    }

    @Override
    public DbExecResult makeQueryResultWithNoRows(long rowsAffected, String message, long lastInsertId, int statusFlags, int warnings) {
        List<Long> ids = lastInsertId > 0 ? singletonList(lastInsertId) : emptyList();
        return new DbQueryResultImpl(rowsAffected, message, null, ids);
    }
}