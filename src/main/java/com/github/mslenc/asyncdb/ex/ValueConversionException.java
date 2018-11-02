package com.github.mslenc.asyncdb.ex;

public class ValueConversionException extends DatabaseException {
    public ValueConversionException() {
        super();
    }

    public ValueConversionException(String message) {
        super(message);
    }

    public ValueConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueConversionException(Throwable cause) {
        super(cause);
    }

    protected ValueConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
