package com.github.mslenc.asyncdb.common.column;

import java.math.BigDecimal;

public class BigDecimalEncoderDecoder implements ColumnEncoderDecoder {
    private static final BigDecimalEncoderDecoder instance = new BigDecimalEncoderDecoder();

    public static BigDecimalEncoderDecoder instance() {
        return instance;
    }

    @Override
    public BigDecimal decode(String value) {
        return new BigDecimal(value);
    }
}
