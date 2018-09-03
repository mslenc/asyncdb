package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.util.HashSet;
import java.util.Set;

public class StringLiteralEncoder implements SqlLiteralEncoder {
    private static final StringLiteralEncoder instance = new StringLiteralEncoder();

    public static StringLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        String string = value.toString();

        out.ensureCapacity(out.length() + string.length() + 10);

        out.append("'");

        string.codePoints().forEachOrdered(cp -> {
            switch (cp) {
                case 0:
                    out.append("\\0");
                    break;

                case 26:
                    out.append("\\Z");
                    break;

                case '\b':
                    out.append("\\b");
                    break;

                case '\t':
                    out.append("\\t");
                    break;

                case '\n':
                    out.append("\\n");
                    break;

                case '\r':
                    out.append("\\r");
                    break;

                case '"':
                    out.append("\\\"");
                    break;

                case '\\':
                    out.append("\\\\");
                    break;

                case '\'':
                    out.append("\\'");
                    break;

                default:
                    out.appendCodePoint(cp);
                    break;
            }
        });

        out.append("'");
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        HashSet<Class<?>> result = new HashSet<>();

        result.add(String.class);
        result.add(CharSequence.class);

        return result;
    }
}
