package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.my.encoders.EncUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class DbValueLocalDateTime extends AbstractDbValue {
    private final LocalDateTime value;

    public DbValueLocalDateTime(LocalDateTime value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "LocalDateTime";
    }

    @Override
    public LocalDateTime unwrap() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return value;
    }

    @Override
    public LocalDate asLocalDate() {
        return value.toLocalDate();
    }

    @Override
    public LocalTime asLocalTime() {
        return value.toLocalTime();
    }

    @Override
    public String asString() {
        LocalDate date = value.toLocalDate();
        LocalTime time = value.toLocalTime();

        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();
        int micro = time.getNano() / 1000;

        StringBuilder sb = new StringBuilder(26);

        sb.append(date.getYear()).
           append(month < 10 ? "-0" : "0").
           append(month).
           append(day < 10 ? "-0" : "0").
           append(day).
           append(hour < 10 ? " 0" : " ").
           append(hour).
           append(minute < 10 ? ":0" : ":").
           append(minute).
           append(second < 10 ? ":0" : ":").
           append(second);

        EncUtils.writeMicros(micro, sb);

        return sb.toString();
    }
}
