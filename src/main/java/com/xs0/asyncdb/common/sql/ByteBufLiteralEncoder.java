package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.Set;

public class ByteBufLiteralEncoder implements SqlLiteralEncoder {
    private static final ByteBufLiteralEncoder instance = new ByteBufLiteralEncoder();

    public static ByteBufLiteralEncoder instance() {
        return instance;
    }

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        ByteBuf bytes = (ByteBuf) value;

        out.append("x'");

        for (int remain = bytes.readableBytes(); remain > 0; remain--) {
            byte b = bytes.readByte();

            out.append(HEX_CHARS[(b & 0xF0) >>> 4]);
            out.append(HEX_CHARS[b & 0x0F]);
        }
        out.append("'");
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(ByteBuf.class);
    }
}
