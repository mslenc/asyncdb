package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;

public class CalendarEncoder implements BinaryEncoder {
    private static final CalendarEncoder instance = new CalendarEncoder();

    public static CalendarEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        Calendar calendar = (Calendar) value;

        long millis = calendar.getTimeInMillis();
        long seconds = millis / 1000;
        int nanos = (int)(millis % 1000) * 1000000;
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(calendar.getTimeZone().getRawOffset() / 1000);
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(seconds, nanos, zoneOffset);

        LocalDateTimeEncoder.instance().encode(localDateTime, buffer);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TIMESTAMP;
    }
}
