package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.ex.ValueConversionException;

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
        byte result = (byte) value;
        if (result == value)
            return result;
        throw new ValueConversionException("Value " + value + " does not fit into a byte");
    }

    @Override
    public short asShort() {
        short result = (short) value;
        if (result == value)
            return result;
        throw new ValueConversionException("Value " + value + " does not fit into a short");
    }

    @Override
    public int asInt() {
        int result = (int) value;
        if (result == value)
            return result;
        throw new ValueConversionException("Value " + value + " does not fit into an int");
    }

    @Override
    public long asLong() {
        long result = (long) value;
        if (result == value)
            return result;
        throw new ValueConversionException("Value " + value + " does not fit into a long");
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
