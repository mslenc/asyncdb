package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

public class DurationEncoder implements BinaryEncoder {
    private static final DurationEncoder instance = new DurationEncoder();

    public static DurationEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        Duration duration = (Duration) value;

        boolean negative = duration.isNegative();
        if (negative)
            duration = duration.negated();

        int days = (int)duration.toDays();
        duration = duration.minus(Duration.ofDays(days));
        int hours = (int)duration.toHours();
        duration = duration.minus(Duration.ofHours(hours));
        int minutes = (int)duration.toMinutes();
        duration = duration.minus(Duration.ofMinutes(minutes));

        long nanos = duration.toNanos();

        int seconds = (int)(nanos / 1000000000L);
        nanos -= seconds * 1000000000L;

        int micros = (int) (nanos / 1000);


        boolean hasMicros = micros != 0;

        buffer.writeByte(hasMicros ? 12 : 8);
        buffer.writeByte(negative ? 1 : 0);

        buffer.writeIntLE(days);
        buffer.writeByte(hours);
        buffer.writeByte(minutes);
        buffer.writeByte(seconds);

        if (hasMicros) {
            buffer.writeIntLE(micros);
        }
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TIME;
    }
}
