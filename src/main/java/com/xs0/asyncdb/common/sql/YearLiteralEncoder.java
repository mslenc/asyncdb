package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.time.Year;
import java.util.Collections;
import java.util.Set;

public class YearLiteralEncoder implements SqlLiteralEncoder {
    private static final YearLiteralEncoder instance = new YearLiteralEncoder();

    public static YearLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        out.append(((Year)value).getValue());
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Year.class);
    }
}
