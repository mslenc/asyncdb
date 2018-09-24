package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

public class DurationLiteralEncoder implements SqlLiteralEncoder {
    private static final DurationLiteralEncoder instance = new DurationLiteralEncoder();

    public static DurationLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, StringBuilder out, CodecSettings settings) {
        Duration duration = (Duration) value;

        out.append("'");

        if (duration.isNegative()) {
            out.append('-');
            duration = duration.negated();
        }

        long seconds = duration.getSeconds();
        long hours = seconds / 3600; seconds %= 3600;
        long minutes = seconds / 60; seconds %= 60;
        long micros = duration.getNano() / 1000;

        out.append(hours / 10);
        out.append(hours % 10);
        out.append(':');
        out.append(minutes / 10);
        out.append(minutes % 10);
        out.append(':');
        out.append(seconds / 10);
        out.append(seconds % 10);

        if (micros != 0) {
            out.append('.');
            int div = 100000;
            while (micros > 0) {
                out.append(micros / div);
                micros %= div;
                div /= 10;
            }
        }

        out.append("'");
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Duration.class);
    }
}
