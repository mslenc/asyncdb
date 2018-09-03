package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

public class BigIntegerLiteralEncoder implements SqlLiteralEncoder {
    private static final BigIntegerLiteralEncoder instance = new BigIntegerLiteralEncoder();

    public static BigIntegerLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        out.append(value);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(BigInteger.class);
    }
}
