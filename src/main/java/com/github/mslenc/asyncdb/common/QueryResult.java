package com.github.mslenc.asyncdb.common;

public interface QueryResult {
    long rowsAffected();
    String statusMessage();
    ResultSet resultSet();
}
