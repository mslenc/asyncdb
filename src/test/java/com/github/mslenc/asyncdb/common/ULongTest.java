package com.github.mslenc.asyncdb.common;

import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;

public class ULongTest {
    private static final long[] consecutiveBitPatterns = new long[2081];
    static {
        int i = 1;

        for (long bits = ~0L; bits != 0; bits >>>= 1) {
            for (long num = bits; Long.bitCount(num) == Long.bitCount(bits); num <<= 1) {
                consecutiveBitPatterns[i++] = num;
            }
        }

        // (sanity check 1 - there are actually 2081 different values)
        HashSet<Long> set = new HashSet<>();
        for (long l : consecutiveBitPatterns)
            set.add(l);
        assertEquals(consecutiveBitPatterns.length, set.size());

        // (sanity check 2 - they contain what the name implies)
        for (long l : consecutiveBitPatterns) {
            assertTrue(Long.toBinaryString(l), Long.toBinaryString(l).matches("^(0)|(1+0*)$"));
        }
    }

    @Test
    public void testDoubleValue() {
        for (long a : consecutiveBitPatterns) {
            for (long b : consecutiveBitPatterns) {
                long num = a ^ b;

                double expected = Double.parseDouble(Long.toUnsignedString(num));
                double actual = ULong.valueOf(num).doubleValue();

                assertEquals(expected, actual, 0.0);
            }
        }
    }

    @Test
    public void testFloatValue() {
        for (long a : consecutiveBitPatterns) {
            for (long b : consecutiveBitPatterns) {
                long num = a ^ b;

                float expected = Float.parseFloat(Long.toUnsignedString(num));
                float actual = ULong.valueOf(num).floatValue();

                assertEquals(expected, actual, 0.0f);
            }
        }
    }

    @Test
    public void testHashcodeEquals() {
        for (long a : consecutiveBitPatterns) {
            for (long b : consecutiveBitPatterns) {
                long num = a ^ b;

                ULong u1 = ULong.valueOf(num);
                ULong u2 = ULong.valueOf(num);
                ULong u3 = ULong.valueOf(num ^ 1);

                assertEquals(u1, u2);
                assertNotEquals(u1, u3);
                assertNotEquals(u2, u3);

                assertEquals(u1.hashCode(), u2.hashCode());
            }
        }
    }

    @Test
    public void testToString() {
        for (long a : consecutiveBitPatterns) {
            for (long b : consecutiveBitPatterns) {
                long num = a ^ b;

                ULong ulong = ULong.valueOf(num);

                assertEquals(Long.toUnsignedString(num, 10), ulong.toString());
            }
        }
    }
}