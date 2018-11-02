package com.github.mslenc.asyncdb.impl.values;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;

public class DbValueYear extends AbstractDbValue {
    private final Year value;

    public DbValueYear(Year value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "Year";
    }

    @Override
    public Year unwrap() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public int asInt() {
        return value.getValue();
    }

    public short asShort() {
        return (short) value.getValue();
    }

    @Override
    public BigDecimal asBigDecimal() {
        return BigDecimal.valueOf(value.getValue());
    }

    @Override
    public String asString() {
        return value.toString();
    }
}
