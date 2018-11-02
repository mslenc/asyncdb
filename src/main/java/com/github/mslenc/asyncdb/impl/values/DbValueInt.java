package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;
import java.time.Year;

public class DbValueInt extends AbstractDbValue {
    private final int value;

    public DbValueInt(int value) {
        this.value = value;
    }

    @Override
    protected String typeName() {
        return "int";
    }

    @Override
    public Integer unwrap() {
        return value;
    }

    @Override
    public String asString() {
        return Integer.toString(value);
    }

    @Override
    public int asInt() {
        return value;
    }

    @Override
    public long asLong() {
        return value;
    }

    @Override
    public ULong asULong() {
        return ULong.valueOf(value & 0xFFFFFFFFL);
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
        return Year.of(value);
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
