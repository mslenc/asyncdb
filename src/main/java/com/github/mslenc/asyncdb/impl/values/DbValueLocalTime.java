package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.my.encoders.EncUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

public class DbValueLocalTime extends AbstractDbValue {
    private final LocalTime value;

    public DbValueLocalTime(LocalTime value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "LocalTime";
    }

    @Override
    public LocalTime unwrap() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public LocalTime asLocalTime() {
        return value;
    }

    @Override
    public Duration asDuration() {
        return Duration.ofSeconds(value.toSecondOfDay(), value.getNano());
    }

    @Override
    public String asString() {
        LocalTime time = value;

        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();
        int micro = time.getNano() / 1000;

        StringBuilder sb = new StringBuilder();

        sb.append(hour < 10 ? "0" : "").
           append(hour).
           append(minute < 10 ? ":0" : ":").
           append(minute).
           append(second < 10 ? ":0" : ":").
           append(second);

        EncUtils.writeMicros(micro, sb);

        return sb.toString();
    }
}
