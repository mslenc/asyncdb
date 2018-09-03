package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.util.Collections;
import java.util.Set;

public class BooleanLiteralEncoder implements SqlLiteralEncoder {
    private static final BooleanLiteralEncoder instance = new BooleanLiteralEncoder();

    public static BooleanLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Boolean bool = (Boolean) value;

        if (bool) {
            out.append(1);
        } else {
            out.append(0);
        }
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Boolean.class);
    }
}
