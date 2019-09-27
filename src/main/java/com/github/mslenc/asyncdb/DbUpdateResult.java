package com.github.mslenc.asyncdb;

public interface DbUpdateResult {
    long getRowsAffected();
    String getStatusMessage();
    DbResultSet getGeneratedIds();
}
