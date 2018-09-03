package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.util.Collections;
import java.util.Set;

public class NullLiteralEncoder implements SqlLiteralEncoder {
    private static final NullLiteralEncoder instance = new NullLiteralEncoder();

    public static NullLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        out.append("NULL");
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.emptySet();
    }
}
