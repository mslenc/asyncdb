package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.ex.ValueConversionException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

public class DbValueDuration extends AbstractDbValue {
    private static final Duration FIRST_INVALID_TIME = Duration.ofSeconds(24 * 3600);

    private final Duration value;

    public DbValueDuration(Duration value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "Duration";
    }

    @Override
    public Duration unwrap() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return !value.isZero();
    }

    @Override
    public Duration asDuration() {
        return value;
    }

    @Override
    public LocalTime asLocalTime() {
        if (value.isNegative() || value.compareTo(FIRST_INVALID_TIME) >= 0)
            throw new ValueConversionException("The TIME value is outside LocalTime range");

        int s = (int) value.getSeconds();
        return LocalTime.of(s / 3600, (s / 60) % 60, s % 60, value.getNano());
    }
}
