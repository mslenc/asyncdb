package com.github.mslenc.asyncdb;

import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;
import java.time.*;

public interface DbValue {
    boolean isNull();
    Object unwrap();

    String asString();

    byte asByte();
    short asShort();
    int asInt();
    long asLong();
    ULong asULong();
    float asFloat();
    double asDouble();
    BigDecimal asBigDecimal();
    Number asNumber();

    LocalDate asLocalDate();
    LocalTime asLocalTime();
    LocalDateTime asLocalDateTime();
    OffsetTime asOffsetTime();
    OffsetDateTime asOffsetDateTime();
    Duration asDuration();
    Instant asInstant();
    Year asYear();

    byte[] asByteArray();

    boolean asBoolean();
}
