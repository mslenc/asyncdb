package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.sql.Date;
import java.util.Collections;
import java.util.Set;

public class SqlDateLiteralEncoder implements SqlLiteralEncoder {
    private static final SqlDateLiteralEncoder instance = new SqlDateLiteralEncoder();

    public static SqlDateLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Date date = (Date)value;
        LocalDateLiteralEncoder.instance().encode(date.toLocalDate(), out, settings);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Date.class);
    }
}

