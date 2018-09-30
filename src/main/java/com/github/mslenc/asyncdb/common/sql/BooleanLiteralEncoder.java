package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.Set;

public class BooleanLiteralEncoder implements SqlLiteralEncoder {
    private static final BooleanLiteralEncoder instance = new BooleanLiteralEncoder();

    public static BooleanLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        Boolean bool = (Boolean) value;

        if (bool) {
            out.writeByte('1');
        } else {
            out.writeByte('0');
        }
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Boolean.class);
    }
}
