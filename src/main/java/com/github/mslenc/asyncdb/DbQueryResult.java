package com.github.mslenc.asyncdb;

import java.util.List;

public interface DbQueryResult {
    long getRowsAffected();
    String getStatusMessage();
    DbResultSet getResultSet();
    List<Long> getGeneratedIds();
}
