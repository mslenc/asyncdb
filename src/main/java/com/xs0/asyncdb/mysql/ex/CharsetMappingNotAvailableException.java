package com.xs0.asyncdb.mysql.ex;

import java.nio.charset.Charset;

public class CharsetMappingNotAvailableException extends RuntimeException{
    private final Charset charset;

    public CharsetMappingNotAvailableException(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }
}
