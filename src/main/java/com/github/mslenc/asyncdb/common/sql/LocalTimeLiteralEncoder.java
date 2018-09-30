package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.Set;

import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.appendAsciiString;
import static java.time.temporal.ChronoField.*;

public class LocalTimeLiteralEncoder implements SqlLiteralEncoder {
    private static final LocalTimeLiteralEncoder instance = new LocalTimeLiteralEncoder();

    public static LocalTimeLiteralEncoder instance() {
        return instance;
    }

    private static DateTimeFormatter formatter =
            new DateTimeFormatterBuilder()
                    .appendValue(HOUR_OF_DAY, 2)
                    .appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR, 2)
                    .appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2)
                    .toFormatter();

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        LocalTime time = (LocalTime) value;
        out.writeByte('\'');
        appendAsciiString(time.format(formatter), out);
        out.writeByte('\'');
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(LocalTime.class);
    }
}
