package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.util.HashSet;
import java.util.Set;

public class NumberLiteralEncoder implements SqlLiteralEncoder {
    private static final NumberLiteralEncoder instance = new NumberLiteralEncoder();

    public static NumberLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        out.append(value);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        HashSet<Class<?>> result = new HashSet<>();

        result.add(Byte.class);
        result.add(Short.class);
        result.add(Integer.class);
        result.add(Long.class);
        result.add(Float.class);
        result.add(Double.class);
        result.add(Number.class);

        return result;
    }
}
