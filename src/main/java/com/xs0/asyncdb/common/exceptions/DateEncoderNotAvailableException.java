package com.xs0.asyncdb.common.exceptions;

public class DateEncoderNotAvailableException extends DatabaseException {
    public DateEncoderNotAvailableException(Object value) {
        super(String.format("There is no encoder for value [%s] of type %s", value, value.getClass().getCanonicalName()));
    }
}