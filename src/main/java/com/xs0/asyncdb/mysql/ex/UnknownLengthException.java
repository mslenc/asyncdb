package com.xs0.asyncdb.mysql.ex;

public class UnknownLengthException extends DecodingException {
    private final int firstByte;

    public UnknownLengthException(int firstByte) {
        this.firstByte = firstByte;
    }

    public int getFirstByte() {
        return firstByte;
    }
}
