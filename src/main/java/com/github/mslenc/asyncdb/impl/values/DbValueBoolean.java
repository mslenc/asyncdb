package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;

public class DbValueBoolean extends AbstractDbValue {
    private final boolean value;
    private static final DbValueBoolean TRUE = new DbValueBoolean(true);
    private static final DbValueBoolean FALSE = new DbValueBoolean(false);

    private DbValueBoolean(boolean value) {
        this.value = value;
    }

    public static DbValueBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    protected String typeName() {
        return "boolean";
    }

    @Override
    public Boolean unwrap() {
        return value;
    }

    @Override
    public String asString() {
        return Boolean.toString(value);
    }

    @Override
    public int asInt() {
        return value ? 1 : 0;
    }

    @Override
    public long asLong() {
        return value ? 1L : 0L;
    }

    @Override
    public ULong asULong() {
        return ULong.valueOf(value ? 1 : 0);
    }

    @Override
    public float asFloat() {
        return value ? 1.0f : 0.0f;
    }

    @Override
    public double asDouble() {
        return value ? 1.0 : 0.0;
    }

    @Override
    public BigDecimal asBigDecimal() {
        return value ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    @Override
    public Number asNumber() {
        return value ? 1 : 0;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }
}
