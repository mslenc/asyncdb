package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.ex.ValueConversionException;

import java.math.BigDecimal;
import java.util.Objects;

public class DbValueBigDecimal extends AbstractDbValue {
    private final BigDecimal value;

    public DbValueBigDecimal(BigDecimal value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "BigDecimal";
    }

    @Override
    public BigDecimal unwrap() {
        return value;
    }

    @Override
    public String asString() {
        return value.toPlainString();
    }

    @Override
    public BigDecimal asBigDecimal() {
        return value;
    }

    @Override
    public Number asNumber() {
        return value;
    }

    @Override
    public byte asByte() {
        try {
            return value.byteValueExact();
        } catch (ArithmeticException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public short asShort() {
        try {
            return value.shortValueExact();
        } catch (ArithmeticException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public int asInt() {
        try {
            return value.intValueExact();
        } catch (ArithmeticException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public long asLong() {
        try {
            return value.longValueExact();
        } catch (ArithmeticException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public float asFloat() {
        float result = value.floatValue();
        if (Float.isInfinite(result))
            throw new ValueConversionException("Value " + value + " is out of range for float");
        return result;
    }

    @Override
    public double asDouble() {
        double result = value.doubleValue();
        if (Double.isInfinite(result))
            throw new ValueConversionException("Value " + value + " is out of range for double");
        return result;
    }

    @Override
    public boolean asBoolean() {
        return value.compareTo(BigDecimal.ZERO) != 0;
    }
}
