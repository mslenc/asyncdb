package com.github.mslenc.asyncdb.mysql.ex;

import com.github.mslenc.asyncdb.common.exceptions.DatabaseException;
import com.github.mslenc.asyncdb.mysql.message.server.ErrorMessage;

public class MySQLException extends DatabaseException {
    private final ErrorMessage err;

    public MySQLException(ErrorMessage err) {
        super("Error " + err.errorCode + " - " + err.sqlState + " - " + err.errorMessage);

        this.err = err;
    }

    public int getErrorCode() {
        return err.errorCode;
    }

    public String getSqlState() {
        return err.sqlState;
    }

    public String getErrorMessage() {
        return err.errorMessage;
    }
}
