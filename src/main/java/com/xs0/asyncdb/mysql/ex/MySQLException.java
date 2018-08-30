package com.xs0.asyncdb.mysql.ex;

import com.xs0.asyncdb.common.exceptions.DatabaseException;
import com.xs0.asyncdb.mysql.message.server.ErrorMessage;

public class MySQLException extends DatabaseException {
    public MySQLException(ErrorMessage err) {
        super("Error " + err.errorCode + " - " + err.sqlState + " - " + err.errorMessage);
    }
}
