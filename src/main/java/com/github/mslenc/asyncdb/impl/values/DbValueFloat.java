package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.ex.ValueConversionException;

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
        int result = (int)value;
        if (result == value)
            return result;
        throw new ValueConversionException("Value " + value + " does not fit into an int");
    }

    @Override
    public long asLong() {
        long result = (long)value;
        if (result == value)
            return result;
        throw new ValueConversionException("Value " + value + " does not fit into a long");
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
