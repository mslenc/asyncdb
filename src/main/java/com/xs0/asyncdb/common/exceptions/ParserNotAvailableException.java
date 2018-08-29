package com.xs0.asyncdb.common.exceptions;

public class ParserNotAvailableException extends DatabaseException {
    public ParserNotAvailableException(int type) {
        super(String.format("There is no parser available for message type '%s' (%s)", type, Integer.toHexString(type)));
    }
}
