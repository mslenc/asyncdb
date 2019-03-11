package com.github.mslenc.asyncdb.impl.values;

import java.time.*;

public class DbValueOffsetDateTime extends AbstractDbValue {
    private final OffsetDateTime value;

    public DbValueOffsetDateTime(OffsetDateTime value) {
        this.value = value;
    }

    @Override
    protected String typeName() {
        return "OffsetDateTime";
    }

    @Override
    public OffsetDateTime unwrap() {
        return value;
    }

    @Override
    public OffsetDateTime asOffsetDateTime() {
        return value;
    }

    @Override
    public OffsetTime asOffsetTime() {
        return value.toOffsetTime();
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return value.toLocalDateTime();
    }

    @Override
    public LocalTime asLocalTime() {
        return value.toLocalTime();
    }

    @Override
    public Instant asInstant() {
        return value.toInstant();
    }

    @Override
    public boolean asBoolean() {
        return true;
    }
}
