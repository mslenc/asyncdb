package com.github.mslenc.asyncdb.common.exceptions;

public class InsufficientParametersException extends DatabaseException {
    public InsufficientParametersException(int expected, int given) {
        super("The query has " + expected + " ? parameters, but " + given + " were given");
    }
}
