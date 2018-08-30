package com.xs0.asyncdb.mysql;

import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.common.ResultSet;
import sun.plugin2.message.Message;

public class MySQLQueryResult implements QueryResult {
    private final long rowsAffected;
    private final String message;
    private final long lastInsertId;
    private final int statusFlags;
    private final int warnings;
    private final ResultSet resultSet;


    public MySQLQueryResult(long rowsAffected,
                            String message,
                            long lastInsertId,
                            int statusFlags,
                            int warnings,
                            ResultSet resultSet) {

        this.rowsAffected = rowsAffected;
        this.message = message;
        this.lastInsertId = lastInsertId;
        this.statusFlags = statusFlags;
        this.warnings = warnings;
        this.resultSet = resultSet;
    }

    @Override
    public long rowsAffected() {
        return rowsAffected;
    }

    @Override
    public String statusMessage() {
        return message;
    }

    @Override
    public ResultSet resultSet() {
        return resultSet;
    }
}
