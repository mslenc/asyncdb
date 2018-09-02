package com.xs0.asyncdb.common.exceptions;

public class ConnectionClosedException extends DatabaseException {
    public ConnectionClosedException() {
        super("The connection was closed");
    }

    public ConnectionClosedException(Throwable cause) {
        super("The connection was closed", cause);
    }
}
