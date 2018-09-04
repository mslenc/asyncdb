package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

import java.util.List;

public class SqlQueryPlaceholders {
    public static String insertValuesForPlaceholders(String query, List<Object> values, SqlLiteralEncoders literalEncoders, CodecSettings settings) {
        StringBuilder out = new StringBuilder(query.length() + values.size() * 20 + 10);

        int mode = 0; // 1 = inside ', 2 = inside ", 3 = inside `, 16 = after \

        int parameterCount = 0;

        for (int i = 0, len = query.length(); i < len; i++) {
            char c = query.charAt(i);

            if (c != '?')
                out.append(c);

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

                        Object value = values.get(parameterCount);
                        SqlLiteralEncoder encoder = literalEncoders.getEncoderFor(value);
                        if (encoder == null)
                            throw new IllegalArgumentException("Unsupported value of class " + value.getClass().getCanonicalName());

                        encoder.encode(value, out, settings);

                        parameterCount++;
                    } else {
                        out.append(c);
                    }
                    break;
            }
        }

        if (parameterCount < values.size())
            throw new IllegalArgumentException("Too many ? values provided");

        return out.toString();
    }
}
