package com.github.mslenc.asyncdb.impl.values;

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
    public String toString() {
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
        return value.byteValue();
    }

    @Override
    public short asShort() {
        return value.shortValue();
    }

    @Override
    public int asInt() {
        return value.intValue();
    }

    @Override
    public long asLong() {
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
        return value.compareTo(BigDecimal.ZERO) != 0;
    }
}
