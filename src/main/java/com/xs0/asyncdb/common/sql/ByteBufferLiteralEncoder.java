package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;

public class ByteBufferLiteralEncoder implements SqlLiteralEncoder {
    private static final ByteBufferLiteralEncoder instance = new ByteBufferLiteralEncoder();

    public static ByteBufferLiteralEncoder instance() {
        return instance;
    }

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        ByteBuffer bytes = (ByteBuffer) value;

        out.append("x'");

        while (bytes.hasRemaining()) {
            byte b = bytes.get();

            out.append(HEX_CHARS[(b & 0xF0) >>> 4]);
            out.append(HEX_CHARS[b & 0x0F]);
        }
        out.append("'");
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(ByteBuffer.class);
    }
}
