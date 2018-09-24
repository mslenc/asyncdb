package com.github.mslenc.asyncdb.common.exceptions;

public class ProtocolException extends DatabaseException {
    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
