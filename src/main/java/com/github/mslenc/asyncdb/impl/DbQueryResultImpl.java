package com.github.mslenc.asyncdb.impl;

import com.github.mslenc.asyncdb.DbQueryResult;
import com.github.mslenc.asyncdb.DbResultSet;

import java.util.Collections;
import java.util.List;

public class DbQueryResultImpl implements DbQueryResult {
    private final long rowsAffected;
    private final String statusMessage;
    private final DbResultSet resultSet;
    private final List<Long> generatedIds;

    public DbQueryResultImpl(long rowsAffected, String statusMessage, DbResultSet resultSet, List<Long> generatedIds) {
        this.rowsAffected = rowsAffected;
        this.statusMessage = statusMessage;
        this.resultSet = resultSet;
        this.generatedIds = generatedIds != null ? generatedIds : Collections.emptyList();
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
    public List<Long> getGeneratedIds() {
        return generatedIds;
    }
}
