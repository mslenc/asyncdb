package com.github.mslenc.asyncdb.impl.values;

import java.time.LocalTime;
import java.time.OffsetTime;

public class DbValueOffsetTime extends AbstractDbValue {
    private final OffsetTime value;

    public DbValueOffsetTime(OffsetTime value) {
        this.value = value;
    }

    @Override
    protected String typeName() {
        return "OffsetTime";
    }

    @Override
    public OffsetTime unwrap() {
        return value;
    }

    @Override
    public OffsetTime asOffsetTime() {
        return value;
    }

    @Override
    public LocalTime asLocalTime() {
        return value.toLocalTime();
    }

    @Override
    public boolean asBoolean() {
        return true;
    }
}
