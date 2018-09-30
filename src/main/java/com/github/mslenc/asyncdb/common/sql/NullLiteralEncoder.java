package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public class NullLiteralEncoder implements SqlLiteralEncoder {
    private static final NullLiteralEncoder instance = new NullLiteralEncoder();

    public static NullLiteralEncoder instance() {
        return instance;
    }

    private static final byte[] NULL = "NULL".getBytes(StandardCharsets.UTF_8);
    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        out.writeBytes(NULL);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.emptySet();
    }
}
