package com.github.mslenc.asyncdb.common.column;

import java.math.BigInteger;

public class BigIntegerEncoderDecoder implements ColumnEncoderDecoder {
    private static final BigIntegerEncoderDecoder instance = new BigIntegerEncoderDecoder();

    public static BigIntegerEncoderDecoder instance() {
        return instance;
    }

    @Override
    public BigInteger decode(String value) {
        return new BigInteger(value);
    }
}
