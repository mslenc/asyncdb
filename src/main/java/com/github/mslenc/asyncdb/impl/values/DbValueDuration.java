package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.ex.ValueConversionException;
import com.github.mslenc.asyncdb.my.encoders.EncUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

import static com.github.mslenc.asyncdb.my.encoders.MyDurationEncoder.adjustedMicros;
import static com.github.mslenc.asyncdb.my.encoders.MyDurationEncoder.adjustedSeconds;

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

    @Override
    public String asString() {
        int totalSeconds = adjustedSeconds(value);
        int micros = adjustedMicros(value);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        StringBuilder out = new StringBuilder(20);

        if (value.isNegative())
            out.append('-');

        if (hours < 10)
            out.append('0');
        out.append(hours);

        out.append(minutes < 10 ? ":0" : ":").append(minutes);
        out.append(seconds < 10 ? ":0" : ":").append(seconds);

        EncUtils.writeMicros(micros, out);

        return out.toString();
    }
}
