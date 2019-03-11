package com.github.mslenc.asyncdb.impl.values;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.util.ULong;
import com.github.mslenc.asyncdb.ex.ValueConversionException;

import java.math.BigDecimal;
import java.time.*;
import java.util.Objects;

public abstract class AbstractDbValue implements DbValue {
    protected abstract String typeName();

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String asString() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a String");
    }

    @Override
    public byte asByte() {
        int val = asInt();

        if (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE)
            throw new ValueConversionException("Value exceeds byte range");

        return (byte) val;
    }

    @Override
    public short asShort() {
        int val = asInt();

        if (val < Short.MIN_VALUE || val > Short.MAX_VALUE)
            throw new ValueConversionException("Value exceeds short range");

        return (short) val;
    }

    @Override
    public int asInt() {
        long val = asLong();

        if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE)
            throw new ValueConversionException("Value exceeds int range");

        return (int) val;
    }

    @Override
    public long asLong() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a long");
    }

    @Override
    public ULong asULong() {
        throw new ValueConversionException("Can't convert " + typeName() + " into an ULong");
    }

    @Override
    public float asFloat() {
        double value = asDouble();
        float floatValue = (float) value;

        if (Float.isNaN(floatValue)) {
            if (Double.isNaN(value)) {
                return floatValue;
            } else {
                // this probably can't really happen..
                throw new ValueConversionException("The value would become a NaN in float");
            }
        }

        if (Float.isInfinite(floatValue)) {
            if (Double.isInfinite(value)) {
                return floatValue;
            } else {
                // .. but this can
                throw new ValueConversionException("The value would become infinite in float");
            }
        }

        if (floatValue == 0.0f) {
            if (value == 0.0) {
                return floatValue;
            } else {
                // while this may seem weird, it's exactly the same as the previous case,
                // except it's about exponent underflow instead of overflow
                throw new ValueConversionException("The value would become 0.0 in float (but is not so in double)");
            }
        }

        return floatValue;
    }

    @Override
    public double asDouble() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a double");
    }

    @Override
    public BigDecimal asBigDecimal() {
        return BigDecimal.valueOf(asLong());
    }

    @Override
    public Number asNumber() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a Number");
    }

    @Override
    public LocalDate asLocalDate() {
        return asLocalDateTime().toLocalDate();
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a LocalDateTime");
    }

    @Override
    public Duration asDuration() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a Duration");
    }

    @Override
    public LocalTime asLocalTime() {
        return asLocalDateTime().toLocalTime();
    }

    @Override
    public Instant asInstant() {
        throw new ValueConversionException("Can't convert " + typeName() + " into an Instant");
    }

    @Override
    public OffsetTime asOffsetTime() {
        throw new ValueConversionException("Can't convert " + typeName() + " into an OffsetTime");
    }

    @Override
    public OffsetDateTime asOffsetDateTime() {
        throw new ValueConversionException("Can't convert " + typeName() + " into an OffsetDateTime");
    }

    @Override
    public Year asYear() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a Year");
    }

    @Override
    public byte[] asByteArray() {
        throw new ValueConversionException("Can't convert " + typeName() + " into a byte[]");
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + unwrap().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Object myValue = unwrap();
        Object otherValue = ((AbstractDbValue)obj).unwrap();

        return Objects.equals(myValue, otherValue);
    }
}
