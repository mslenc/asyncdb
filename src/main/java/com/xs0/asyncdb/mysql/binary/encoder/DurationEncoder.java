package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

public class DurationEncoder implements BinaryEncoder {
    @Override
    public void encode(Object value, ByteBuf buffer) {
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

        buffer.writeInt(days);
        buffer.writeByte(hours);
        buffer.writeByte(minutes);
        buffer.writeByte(seconds);

        if (hasMicros) {
            buffer.writeInt(micros);
        }
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_TIME;
    }
}
