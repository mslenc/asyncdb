package com.github.mslenc.asyncdb.common;

public final class ULong extends Number implements Comparable<ULong> {
    public static final ULong MIN_VALUE = new ULong(0);
    public static final ULong MAX_VALUE = new ULong(0xFFFFFFFFFFFFFFFFL);

    private final long value;

    public ULong(long value) {
        this.value = value;
    }

    public ULong(String s) throws NumberFormatException {
        this.value = Long.parseUnsignedLong(s, 10);
    }

    public static ULong parseULong(String s) throws NumberFormatException {
        return valueOf(Long.parseUnsignedLong(s));
    }

    public static ULong parseULong(String s, int radix) throws NumberFormatException {
        return valueOf(Long.parseUnsignedLong(s, radix));
    }

    public static ULong valueOf(long value) {
        // TODO: cache?
        return new ULong(value);
    }

    public static ULong valueOf(String value) {
        return parseULong(value);
    }

    @Override
    public String toString() {
        return Long.toUnsignedString(value, 10);
    }

    @Override
    public int compareTo(ULong o) {
        return Long.compareUnsigned(value, o.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ULong) {
            return value == ((ULong) obj).value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public int intValue() {
        return (int)value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        if (value >= 0)
            return (float)value;

        return 2.0f * (float)(value >>> 1);
    }

    @Override
    public double doubleValue() {
        if (value >= 0)
            return (double)value;

        return 2.0 * (double)(value >>> 1);
    }
}
