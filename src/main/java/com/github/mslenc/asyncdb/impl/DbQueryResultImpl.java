package com.github.mslenc.asyncdb.impl;

import com.github.mslenc.asyncdb.DbExecResult;
import com.github.mslenc.asyncdb.DbResultSet;
import com.github.mslenc.asyncdb.util.EmptyResultSet;

public class DbQueryResultImpl implements DbExecResult {
    private final long rowsAffected;
    private final String statusMessage;
    private final DbResultSet resultSet;
    private final DbResultSet generatedIds;

    public DbQueryResultImpl(long rowsAffected, String statusMessage, DbResultSet resultSet, DbResultSet generatedIds) {
        this.rowsAffected = rowsAffected;
        this.statusMessage = statusMessage;
        this.resultSet = resultSet;
        this.generatedIds = generatedIds != null ? generatedIds : EmptyResultSet.INSTANCE;
    }

    @Override
    public long getRowsAffected() {
        return rowsAffected;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public DbResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public DbResultSet getGeneratedIds() {
        return generatedIds;
    }
}
