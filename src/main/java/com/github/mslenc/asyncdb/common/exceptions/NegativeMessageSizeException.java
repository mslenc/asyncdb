package com.github.mslenc.asyncdb.common.exceptions;

public class NegativeMessageSizeException extends DatabaseException {
    public NegativeMessageSizeException(int code, int size) {
        super(String.format("Message of type %d had negative size %s", code, size));
    }
}
