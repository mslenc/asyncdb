package com.github.mslenc.asyncdb.util;

import io.netty.buffer.ByteBuf;

import java.util.List;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.appendUtf8Codepoint;

public class SqlQueryPlaceholders {
    public interface SqlLiteralEncoder {
        <T> void encodeValue(T value, ByteBuf out);
    }

    public static void insertValuesForPlaceholders(String query, List<Object> values, SqlLiteralEncoder encoder, ByteBuf out) {
        int mode = 0; // 1 = inside ', 2 = inside ", 3 = inside `, 16 = after \

        int parameterCount = 0;

        int len = query.length();
        int i = 0;
        while (i < len) {
            int c = query.codePointAt(i);
            if (c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i += 2;
            } else {
                i += 1;
            }

            if (c != '?')
                appendUtf8Codepoint(c, out);

            if ((mode & 16) == 16) { // after backslash
                mode ^= 16;
                continue;
            }

            switch (c) {
                case '\\':
                    if (mode == 0) { // outside a string.. who knows what the \ is supposed to be..
                        // ignore
                    } else {
                        mode |= 16; // enter escape mode, next char handled above..
                    }
                    break;

                case '\'':
                    if (mode == 0) {
                        mode = 1;
                    } else
                    if (mode == 1) {
                        mode = 0;
                    }
                    break;

                case '"':
                    if (mode == 0) {
                        mode = 2;
                    } else
                    if (mode == 2) {
                        mode = 0;
                    }
                    break;

                case '`':
                    if (mode == 0) {
                        mode = 3;
                    } else
                    if (mode == 3) {
                        mode = 0;
                    }
                    break;

                case '?':
                    if (mode == 0) {
                        if (parameterCount >= values.size())
                            throw new IllegalArgumentException("Too few ? values provided");

                        Object value = values.get(parameterCount++);

                        encoder.encodeValue(value, out);
                    } else {
                        out.writeByte('?');
                    }
                    break;
            }
        }

        if (parameterCount < values.size())
            throw new IllegalArgumentException("Too many ? values provided");
    }
}
