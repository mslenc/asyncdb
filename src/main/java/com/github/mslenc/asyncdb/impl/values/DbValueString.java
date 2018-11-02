package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.util.ULong;
import com.github.mslenc.asyncdb.ex.ValueConversionException;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DbValueString extends AbstractDbValue {
    private final String value;

    public DbValueString(String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected String typeName() {
        return "String";
    }

    @Override
    public String unwrap() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return !value.isEmpty();
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public byte asByte() {
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public short asShort() {
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public int asInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public long asLong() {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public ULong asULong() {
        try {
            return ULong.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public float asFloat() {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public double asDouble() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public BigDecimal asBigDecimal() {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public Number asNumber() {
        return asBigDecimal();
    }

    @Override
    public LocalDate asLocalDate() {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public Duration asDuration() {
        try {
            return Duration.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public LocalTime asLocalTime() {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public Instant asInstant() {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public Year asYear() {
        try {
            return Year.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValueConversionException(e);
        }
    }

    @Override
    public byte[] asByteArray() {
        return value.getBytes(UTF_8);
    }
}
