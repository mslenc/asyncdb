package com.github.mslenc.asyncdb.ex;

public class ConnectionClosedException extends DatabaseException {
    public ConnectionClosedException() {
        super("The connection was closed");
    }

    public ConnectionClosedException(Throwable cause) {
        super("The connection was closed", cause);
    }

    public ConnectionClosedException(String message) {
        super(message);
    }
}
