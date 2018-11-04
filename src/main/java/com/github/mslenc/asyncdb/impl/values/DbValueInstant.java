package com.github.mslenc.asyncdb.impl.values;

import java.time.*;
import java.util.Objects;

public class DbValueInstant extends AbstractDbValue {
    private final Instant timestamp;
    private final LocalDateTime sourceLocalDateTime;

    public DbValueInstant(Instant timestamp, LocalDateTime sourceLocalDateTime) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.sourceLocalDateTime = Objects.requireNonNull(sourceLocalDateTime);
    }

    @Override
    protected String typeName() {
        return "Instant";
    }

    @Override
    public Instant unwrap() {
        return timestamp;
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public Instant asInstant() {
        return timestamp;
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return sourceLocalDateTime;
    }

    @Override
    public LocalTime asLocalTime() {
        return sourceLocalDateTime.toLocalTime();
    }

    @Override
    public LocalDate asLocalDate() {
        return sourceLocalDateTime.toLocalDate();
    }

    @Override
    public Year asYear() {
        return Year.of(sourceLocalDateTime.getYear());
    }

    @Override
    public String asString() {
        return sourceLocalDateTime.toLocalDate() + " " + sourceLocalDateTime.toLocalTime();
    }
}
