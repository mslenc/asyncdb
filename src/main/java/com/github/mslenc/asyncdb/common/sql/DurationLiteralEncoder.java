package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.appendAsciiInteger;

public class DurationLiteralEncoder implements SqlLiteralEncoder {
    private static final DurationLiteralEncoder instance = new DurationLiteralEncoder();

    public static DurationLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        Duration duration = (Duration) value;

        out.writeByte('\'');

        if (duration.isNegative()) {
            out.writeByte('-');
            duration = duration.negated();
        }

        long seconds = duration.getSeconds();
        long hours = seconds / 3600; seconds %= 3600;
        int minutes = (int)(seconds / 60); seconds %= 60;
        int micros = duration.getNano() / 1000;

        appendAsciiInteger(hours / 10, out);
        out.writeByte('0' + (int)(hours % 10));
        out.writeByte(':');
        out.writeByte('0' + (minutes / 10));
        out.writeByte('0' + (minutes % 10));
        out.writeByte(':');
        out.writeByte('0' + (int)(seconds / 10));
        out.writeByte('0' + (int)(seconds % 10));

        if (micros != 0) {
            out.writeByte('.');
            int div = 100000;
            while (micros > 0) {
                out.writeByte('0' + (micros / div));
                micros %= div;
                div /= 10;
            }
        }

        out.writeByte('\'');
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Duration.class);
    }
}
