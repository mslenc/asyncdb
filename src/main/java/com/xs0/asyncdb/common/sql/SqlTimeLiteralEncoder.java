package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.sql.Time;
import java.util.Collections;
import java.util.Set;

public class SqlTimeLiteralEncoder implements SqlLiteralEncoder {
    private static final SqlTimeLiteralEncoder instance = new SqlTimeLiteralEncoder();

    public static SqlTimeLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Time time = (Time) value;
        LocalTimeLiteralEncoder.instance().encode(time.toLocalTime(), out, settings);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Time.class);
    }
}
