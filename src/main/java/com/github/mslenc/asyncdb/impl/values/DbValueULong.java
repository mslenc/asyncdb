package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.util.ULong;
import com.github.mslenc.asyncdb.ex.ValueConversionException;

import java.math.BigDecimal;
import java.util.Objects;

public class DbValueULong extends AbstractDbValue {
    private final ULong value;

    public DbValueULong(ULong value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "ULong";
    }

    @Override
    public String asString() {
        return value.toString();
    }

    @Override
    public ULong unwrap() {
        return value;
    }

    @Override
    public BigDecimal asBigDecimal() {
        if (value.outsideLongRange()) {
            return new BigDecimal(value.toString());
        } else {
            return BigDecimal.valueOf(value.longValue());
        }
    }

    @Override
    public ULong asNumber() {
        return value;
    }

    @Override
    public int asInt() {
        long l = value.longValue();

        if (l < 0 || l > Integer.MAX_VALUE)
            throw new ValueConversionException("Value exceeds int range");

        return (int) l;
    }

    @Override
    public long asLong() {
        if (value.outsideLongRange())
            throw new ValueConversionException("Value exceeds long range");

        return value.longValue();
    }

    @Override
    public float asFloat() {
        return value.floatValue();
    }

    @Override
    public double asDouble() {
        return value.doubleValue();
    }

    @Override
    public boolean asBoolean() {
        return value.longValue() != 0;
    }
}
