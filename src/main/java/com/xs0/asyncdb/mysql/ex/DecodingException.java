package com.xs0.asyncdb.mysql.ex;

public class DecodingException extends RuntimeException {
    public DecodingException() {
        super();
    }

    public DecodingException(String message) {
        super(message);
    }

    public DecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodingException(Throwable cause) {
        super(cause);
    }

    protected DecodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
