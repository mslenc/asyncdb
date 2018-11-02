package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;
import java.time.*;

public class DbValueLong extends AbstractDbValue {
    private final long value;

    public DbValueLong(long value) {
        this.value = value;
    }

    @Override
    protected String typeName() {
        return "long";
    }

    @Override
    public Long unwrap() {
        return value;
    }

    @Override
    public String asString() {
        return Long.toString(value);
    }

    @Override
    public byte asByte() {
        return (byte)value;
    }

    @Override
    public short asShort() {
        return (short)value;
    }

    @Override
    public int asInt() {
        return (int)value;
    }

    @Override
    public long asLong() {
        return value;
    }

    @Override
    public ULong asULong() {
        return ULong.valueOf(value);
    }

    @Override
    public float asFloat() {
        return (float)value;
    }

    @Override
    public double asDouble() {
        return (double)value;
    }

    @Override
    public BigDecimal asBigDecimal() {
        return BigDecimal.valueOf(value);
    }

    @Override
    public Year asYear() {
        return Year.of((int)value);
    }

    @Override
    public Number asNumber() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return value != 0;
    }
}
