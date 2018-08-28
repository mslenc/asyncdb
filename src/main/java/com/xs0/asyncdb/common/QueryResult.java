package com.xs0.asyncdb.common;

public interface QueryResult {
    long rowsAffected();
    String statusMessage();
    ResultSet resultSet();
}
