package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;

import java.util.Collections;
import java.util.Set;

public class ByteArrayLiteralEncoder implements SqlLiteralEncoder {
    private static final ByteArrayLiteralEncoder instance = new ByteArrayLiteralEncoder();

    public static ByteArrayLiteralEncoder instance() {
        return instance;
    }

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        byte[] bytes = (byte[]) value;

        out.append("x'");
        for (byte b : bytes) {
            out.append(HEX_CHARS[(b & 0xF0) >>> 4]);
            out.append(HEX_CHARS[b & 0x0F]);
        }
        out.append("'");
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(byte[].class);
    }
}
