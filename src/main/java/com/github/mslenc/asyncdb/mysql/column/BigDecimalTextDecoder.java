package com.github.mslenc.asyncdb.mysql.column;

import java.math.BigDecimal;

public class BigDecimalTextDecoder implements TextValueDecoder {
    private static final BigDecimalTextDecoder instance = new BigDecimalTextDecoder();

    public static BigDecimalTextDecoder instance() {
        return instance;
    }

    @Override
    public BigDecimal decode(String value) {
        return new BigDecimal(value);
    }
}
