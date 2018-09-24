package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Set;

public class SqlTimestampLiteralEncoder implements SqlLiteralEncoder {
    private static final SqlTimestampLiteralEncoder instance = new SqlTimestampLiteralEncoder();

    public static SqlTimestampLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Timestamp timestamp = (Timestamp) value;
        InstantLiteralEncoder.instance().encode(timestamp.toInstant(), out, settings);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Timestamp.class);
    }
}
