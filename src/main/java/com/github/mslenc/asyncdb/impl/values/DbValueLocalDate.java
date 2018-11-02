package com.github.mslenc.asyncdb.impl.values;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Objects;

public class DbValueLocalDate extends AbstractDbValue {
    private final LocalDate value;

    public DbValueLocalDate(LocalDate value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "LocalDate";
    }

    @Override
    public LocalDate unwrap() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public LocalDate asLocalDate() {
        return value;
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return value.atTime(12, 0, 0);
    }

    @Override
    public Year asYear() {
        return Year.of(value.getYear());
    }

    @Override
    public String asString() {
        int month = value.getMonthValue();
        int day = value.getDayOfMonth();

        return new StringBuilder(10).
            append(value.getYear()). // always 1000-9999
            append(month < 10 ? "-0" : "0").
            append(month).
            append(day < 10 ? "-0" : "0").
            append(day).
            toString();
    }
}
