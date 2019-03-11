package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;
import java.time.*;

public class DbValueNull implements DbValue {
    private static final DbValueNull instance = new DbValueNull();

    public static DbValueNull instance() {
        return instance;
    }

    private DbValueNull() {
        // singleton
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public Object unwrap() {
        return null;
    }

    @Override
    public String asString() {
        return null;
    }

    @Override
    public byte asByte() {
        return 0;
    }

    @Override
    public short asShort() {
        return 0;
    }

    @Override
    public int asInt() {
        return 0;
    }

    @Override
    public long asLong() {
        return 0;
    }

    @Override
    public ULong asULong() {
        return null;
    }

    @Override
    public float asFloat() {
        return 0;
    }

    @Override
    public double asDouble() {
        return 0;
    }

    @Override
    public BigDecimal asBigDecimal() {
        return null;
    }

    @Override
    public Number asNumber() {
        return null;
    }

    @Override
    public LocalDate asLocalDate() {
        return null;
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return null;
    }

    @Override
    public Duration asDuration() {
        return null;
    }

    @Override
    public LocalTime asLocalTime() {
        return null;
    }

    @Override
    public Instant asInstant() {
        return null;
    }

    @Override
    public Year asYear() {
        return null;
    }

    @Override
    public OffsetTime asOffsetTime() {
        return null;
    }

    @Override
    public OffsetDateTime asOffsetDateTime() {
        return null;
    }

    @Override
    public byte[] asByteArray() {
        return null;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public int hashCode() {
        return 58175245;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
