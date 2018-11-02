package com.github.mslenc.asyncdb.ex;

public class ProtocolException extends DatabaseException {
    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolException() {
        super();
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    protected ProtocolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
