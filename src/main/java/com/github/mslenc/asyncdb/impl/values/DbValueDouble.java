package com.github.mslenc.asyncdb.impl.values;

import java.math.BigDecimal;

public class DbValueDouble extends AbstractDbValue {
    private final double value;

    public DbValueDouble(double value) {
        this.value = value;
    }

    @Override
    protected String typeName() {
        return "double";
    }

    @Override
    public Double unwrap() {
        return value;
    }

    @Override
    public String asString() {
        return Double.toString(value);
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
        return (long)value;
    }

    @Override
    public float asFloat() {
        return (float)value;
    }

    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public BigDecimal asBigDecimal() {
        return BigDecimal.valueOf(value);
    }

    @Override
    public Number asNumber() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return !Double.isNaN(value) && value != 0;
    }
}
