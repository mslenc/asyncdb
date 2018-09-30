package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

public class StringLiteralEncoder implements SqlLiteralEncoder {
    private static final StringLiteralEncoder instance = new StringLiteralEncoder();

    public static StringLiteralEncoder instance() {
        return instance;
    }

    private static final int BACKSLASH = '\\' << 8;

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        CharSequence string = (CharSequence)value;

        out.writeByte('\'');
        string.codePoints().forEachOrdered(cp -> appendWithEscaping(cp, out));
        out.writeByte('\'');
    }

    static void appendWithEscaping(int codepoint, ByteBuf out) {
        switch (codepoint) {
            case 0:
                out.writeShort(BACKSLASH | '0');
                break;

            case 26:
                out.writeShort(BACKSLASH | 'Z');
                break;

            case '\b':
                out.writeShort(BACKSLASH | 'b');
                break;

            case '\t':
                out.writeShort(BACKSLASH | 't');
                break;

            case '\n':
                out.writeShort(BACKSLASH | 'n');
                break;

            case '\r':
                out.writeShort(BACKSLASH | 'r');
                break;

            case '"':
                out.writeShort(BACKSLASH | '"');
                break;

            case '\\':
                out.writeShort(BACKSLASH | '\\');
                break;

            case '\'':
                out.writeShort(BACKSLASH | '\'');
                break;

            default:
                ByteBufUtils.appendUtf8Codepoint(codepoint, out);
                break;
        }
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        HashSet<Class<?>> result = new HashSet<>();

        result.add(String.class);
        result.add(CharSequence.class);

        return result;
    }
}
