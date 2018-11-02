package com.github.mslenc.asyncdb.impl.values;

import java.math.BigDecimal;

public class DbValueFloat extends AbstractDbValue {
    private final float value;

    public DbValueFloat(float value) {
        this.value = value;
    }

    @Override
    protected String typeName() {
        return "float";
    }

    @Override
    public boolean asBoolean() {
        return !Float.isNaN(value) && value != 0.0f;
    }

    @Override
    public Float unwrap() {
        return value;
    }

    @Override
    public String asString() {
        return Float.toString(value);
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
        return value;
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
}
