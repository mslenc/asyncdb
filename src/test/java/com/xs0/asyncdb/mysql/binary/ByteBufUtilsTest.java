package com.xs0.asyncdb.mysql.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.*;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.*;

public class ByteBufUtilsTest {
    @Test
    public void testToHexString() {
        assertTrue("cafebabe".equalsIgnoreCase(
            toHexString(new byte[] {(byte)  0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe})));

        assertTrue("".equalsIgnoreCase(toHexString(new byte[0])));

        assertTrue("0000000000".equalsIgnoreCase(toHexString(new byte[] { 0, 0, 0, 0 ,0 })));

        try {
            toHexString(null);
            fail("Should've thrown NPE");
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testIsNullBitSet() {
        byte[] nullBytes = { 0b0011_0110, (byte) 0b1000_1001 };
        //                     7654 3210                  98   etc, bytes from left to right, bits from right to left

        String expect = "0110110010010001";

        for (int i = 0; i < expect.length(); i++) {
            boolean expectBit = expect.charAt(i) == '1';

            for (int offset = 0; offset <= i; offset++) {
                assertEquals(expectBit, isNullBitSet(offset, nullBytes, i - offset));
            }
        }
    }

    static int countOneBits(byte[] bytes) {
        int result = 0;
        for (byte b : bytes)
            result += Integer.bitCount(b & 0xFF);
        return result;
    }

    @Test
    public void testSetNullBit() {
        byte[] bytes = new byte[6];

        assertEquals(0, countOneBits(bytes));

        setNullBit(0, bytes, 0);
        assertTrue(isNullBitSet(0, bytes, 0));
        assertEquals(1, countOneBits(bytes));

        setNullBit(2, bytes, 3);
        assertTrue(isNullBitSet(0, bytes, 0));
        assertTrue(isNullBitSet(2, bytes, 3));
        assertEquals(2, countOneBits(bytes));

        setNullBit(0, bytes, 7);
        assertTrue(isNullBitSet(0, bytes, 0));
        assertTrue(isNullBitSet(2, bytes, 3));
        assertTrue(isNullBitSet(0, bytes, 7));
        assertEquals(3, countOneBits(bytes));

        setNullBit(2, bytes, 45);
        assertTrue(isNullBitSet(0, bytes, 0));
        assertTrue(isNullBitSet(2, bytes, 3));
        assertTrue(isNullBitSet(0, bytes, 7));
        assertTrue(isNullBitSet(2, bytes, 45));
        assertEquals(4, countOneBits(bytes));

        assertEquals(0b1010_0001, bytes[0] & 0xFF);
        assertEquals(0b0000_0000, bytes[1] & 0xFF);
        assertEquals(0b0000_0000, bytes[2] & 0xFF);
        assertEquals(0b0000_0000, bytes[3] & 0xFF);
        assertEquals(0b0000_0000, bytes[4] & 0xFF);
        assertEquals(0b1000_0000, bytes[5] & 0xFF);
    }

    @Test
    public void testReadFixedBytes() {
        Random rnd = new Random();
        for (int len = 0; len < 500; len += 13) {
            byte[] bytes = new byte[len];
            rnd.nextBytes(bytes);

            for (int read = 0; read <= len + 10; read += rnd.nextInt(17)) {
                ByteBuf buf = Unpooled.buffer(len + 100);
                int zeros = rnd.nextInt(100);
                buf.writeZero(zeros);
                buf.skipBytes(zeros);

                buf.writeBytes(bytes);

                if (read <= len)
                    buf.writeZero(rnd.nextInt(100));

                byte[] readBack = readFixedBytes(buf, read);

                if (read > len) {
                    assertNull(readBack);
                } else {
                    assertTrue(Arrays.equals(Arrays.copyOfRange(bytes, 0, read), readBack));
                }
            }
        }
    }

    @Test
    public void testWriteCString() {
        ByteBuf buf1 = Unpooled.buffer();
        ByteBuf buf2 = Unpooled.buffer();
        ByteBuf buf3 = Unpooled.buffer();
        ByteBuf buf4 = Unpooled.buffer();
        ByteBuf buf5 = Unpooled.buffer();
        ByteBuf buf6 = Unpooled.buffer();

        writeCString(buf1, "", UTF_8);
        writeCString(buf2, "123", UTF_8);
        writeCString(buf3, "lorem ipsum sit ", UTF_8);
        writeCString(buf4, "0\u007f\u00FF", UTF_8);
        writeCString(buf5, "Mitja Šlenc", UTF_8);
        writeCString(buf6, "\uD83D\uDEC8", UTF_8);

        byte[] expect1 = { 0 };
        byte[] expect2 = { '1', '2', '3', 0 };
        byte[] expect3 = { 'l', 'o', 'r', 'e', 'm', ' ', 'i', 'p', 's', 'u', 'm', ' ', 's', 'i', 't', ' ', 0 };
        byte[] expect4 = { '0', 0x7F, (byte) 0xC3, (byte) 0xBF, 0 };
        byte[] expect5 = { 'M', 'i', 't', 'j', 'a', ' ', (byte) 0xC5, (byte) 0xA0, 'l', 'e', 'n', 'c', 0 };
        byte[] expect6 = { (byte) 0xF0, (byte) 0x9F, (byte) 0x9B, (byte) 0x88, 0 };

        byte[] actual1 = new byte[buf1.readableBytes()]; buf1.readBytes(actual1);
        byte[] actual2 = new byte[buf2.readableBytes()]; buf2.readBytes(actual2);
        byte[] actual3 = new byte[buf3.readableBytes()]; buf3.readBytes(actual3);
        byte[] actual4 = new byte[buf4.readableBytes()]; buf4.readBytes(actual4);
        byte[] actual5 = new byte[buf5.readableBytes()]; buf5.readBytes(actual5);
        byte[] actual6 = new byte[buf6.readableBytes()]; buf6.readBytes(actual6);

        assertTrue("", Arrays.equals(expect1, actual1));
        assertTrue("123", Arrays.equals(expect2, actual2));
        assertTrue("lorem ipsum sit ", Arrays.equals(expect3, actual3));
        assertTrue("0\u007f\u00FF", Arrays.equals(expect4, actual4));
        assertTrue("Mitja Šlenc", Arrays.equals(expect5, actual5));
        assertTrue("\uD83D\uDEC8", Arrays.equals(expect6, actual6));
    }

    @Test
    public void testReadUntilEOFOrZero() {
        String[] tests = { "", "abc", "mysql_native_password" };
        for (String expected : tests) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeCharSequence(expected, UTF_8);
            String readBack = readUntilEOFOrZero(buf, UTF_8);
            assertEquals(expected, readBack);
            assertEquals(expected.length(), buf.readerIndex());

            ByteBuf buf2 = Unpooled.buffer();
            buf2.writeCharSequence(expected, UTF_8);
            buf2.writeByte(0);
            String readBack2 = readUntilEOFOrZero(buf2, UTF_8);
            assertEquals(expected, readBack2);
            assertEquals(expected.length() + 1, buf2.readerIndex());

            ByteBuf buf3 = Unpooled.buffer();
            buf3.writeCharSequence(expected, UTF_8);
            buf3.writeByte(0);
            buf3.writeBytes(new byte[] { 'a', 'b', 'c' });
            String readBack3 = readUntilEOFOrZero(buf3, UTF_8);
            assertEquals(expected, readBack3);
            assertEquals(expected.length() + 1, buf3.readerIndex());
        }
    }

    @Test
    public void testReadUntilEOF() {
        String[] tests = { "", "abc", "mysql_native_password" };
        for (String expected : tests) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeCharSequence(expected, UTF_8);
            String readBack = readUntilEOF(buf, UTF_8);
            assertEquals(expected, readBack);
            assertEquals(0, buf.readableBytes());

            ByteBuf buf2 = Unpooled.buffer();
            buf2.writeCharSequence(expected, UTF_8);
            buf2.writeByte(0);
            String readBack2 = readUntilEOF(buf2, UTF_8);
            assertEquals(expected + "\u0000", readBack2);
            assertEquals(0, buf2.readableBytes());

            ByteBuf buf3 = Unpooled.buffer();
            buf3.writeCharSequence(expected, UTF_8);
            buf3.writeByte(0);
            buf3.writeBytes(new byte[] { 'a', 'b', 'c' });
            String readBack3 = readUntilEOF(buf3, UTF_8);
            assertEquals(expected + "\u0000abc", readBack3);
            assertEquals(0, buf3.readableBytes());
        }
    }

    @Test
    public void testReadCString() {
        String[] tests = { "", "abc", "mysql_native_password" };
        for (String expected : tests) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeCharSequence(expected, UTF_8);
            String readBack = readCString(buf, UTF_8);
            assertNull(readBack);


            ByteBuf buf2 = Unpooled.buffer();
            buf2.writeCharSequence(expected, UTF_8);
            buf2.writeByte(0);
            String readBack2 = readCString(buf2, UTF_8);
            assertEquals(expected, readBack2);
            assertEquals(0, buf2.readableBytes());

            ByteBuf buf3 = Unpooled.buffer();
            buf3.writeCharSequence(expected, UTF_8);
            buf3.writeByte(0);
            buf3.writeBytes(new byte[] { 'a', 'b', 'c' });
            String readBack3 = readCString(buf3, UTF_8);
            assertEquals(expected, readBack3);
            assertEquals(3, buf3.readableBytes());
        }
    }

    static byte[] bytes(int... ints) {
        // to avoid all the (byte) casts
        byte[] bytes = new byte[ints.length];
        for (int a = 0; a < ints.length; a++)
            bytes[a] = (byte) ints[a];
        return bytes;
    }

    @Test
    public void testWriteLength() {
        LinkedHashMap<Long, byte[]> tests = new LinkedHashMap<>();

        tests.put(              0x00L, bytes(0x00));
        tests.put(              0x01L, bytes(0x01));
        tests.put(              0x50L, bytes(0x50));
        tests.put(              0xF9L, bytes(0xF9));
        tests.put(              0xFAL, bytes(0xFA));
        tests.put(              0xFBL, bytes(0xFC, 0xFB, 0x00));
        tests.put(              0xFCL, bytes(0xFC, 0xFC, 0x00));
        tests.put(              0xFDL, bytes(0xFC, 0xFD, 0x00));
        tests.put(              0xFEL, bytes(0xFC, 0xFE, 0x00));
        tests.put(              0xFFL, bytes(0xFC, 0xFF, 0x00));
        tests.put(             0x100L, bytes(0xFC, 0x00, 0x01));
        tests.put(            0xFFFEL, bytes(0xFC, 0xFE, 0xFF));
        tests.put(            0xFFFFL, bytes(0xFC, 0xFF, 0xFF));
        tests.put(           0x10000L, bytes(0xFD, 0x00, 0x00, 0x01));
        tests.put(           0x10001L, bytes(0xFD, 0x01, 0x00, 0x01));
        tests.put(          0xFFFFFEL, bytes(0xFD, 0xFE, 0xFF, 0xFF));
        tests.put(          0xFFFFFFL, bytes(0xFD, 0xFF, 0xFF, 0xFF));
        tests.put(         0x1000000L, bytes(0xFE, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00));
        tests.put(         0x1000003L, bytes(0xFE, 0x03, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00));
        tests.put(      0x4912849124L, bytes(0xFE, 0x24, 0x91, 0x84, 0x12, 0x49, 0x00, 0x00, 0x00));
        tests.put(      0x7FFFFFFFFFL, bytes(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F, 0x00, 0x00, 0x00));
        tests.put(      0xFFFFFFFFFFL, bytes(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00));
        tests.put(    0x2384CDBD922FL, bytes(0xFE, 0x2F, 0x92, 0xBD, 0xCD, 0x84, 0x23, 0x00, 0x00));
        tests.put(  0x9CBADB9CD71342L, bytes(0xFE, 0x42, 0x13, 0xD7, 0x9C, 0xDB, 0xBA, 0x9C, 0x00));
        tests.put(0x326DBC49F2DCB2BDL, bytes(0xFE, 0xBD, 0xB2, 0xDC, 0xF2, 0x49, 0xBC, 0x6D, 0x32));
        tests.put(0xFFFFFFFFFFFFFFFFL, bytes(0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));

        for (Map.Entry<Long, byte[]> entry : tests.entrySet()) {
            Long value = entry.getKey();
            byte[] expected = entry.getValue();

            ByteBuf buf = Unpooled.buffer();
            writeLength(value, buf);

            byte[] actual = new byte[buf.readableBytes()];
            buf.readBytes(actual);

            assertTrue(Long.toHexString(value) + ": " + toHexString(expected) + " vs " + toHexString(actual),
                       Arrays.equals(expected, actual));
        }
    }

    @Test
    public void testWriteLengthEncodedString() {
        LinkedHashMap<String, byte[]> tests = new LinkedHashMap<>();

        tests.put("", bytes(0));
        tests.put("abc", bytes(3, 'a', 'b', 'c'));
        tests.put("Iñtërnâtiônàlizætiøn", bytes(27, 'I', 0xC3, 0xB1, 't', 0xC3, 0xAB, 'r', 'n', 0xC3, 0xA2, 't', 'i',
                                                    0xC3, 0xB4, 'n', 0xC3, 0xA0, 'l', 'i','z', 0xC3, 0xA6, 't', 'i',
                                                    0xC3, 0xB8, 'n'));

        for (Map.Entry<String, byte[]> entry : tests.entrySet()) {
            String text = entry.getKey();
            byte[] expected = entry.getValue();

            ByteBuf buf = Unpooled.buffer();
            writeLengthEncodedString(text, UTF_8, buf);

            byte[] actual = new byte[buf.readableBytes()];
            buf.readBytes(actual);

            assertTrue(text + ": " + toHexString(expected) + " vs " + toHexString(actual),
                       Arrays.equals(expected, actual));
        }

        // let's also check choosing charset is not ignored
        ByteBuf buf = Unpooled.buffer();
        writeLengthEncodedString("Iñtërnâtiônàlizætiøn", ISO_8859_1, buf);
        byte[] actual = new byte[buf.readableBytes()];
        buf.readBytes(actual);

        byte[] expected = bytes(20, 'I', 'ñ', 't', 'ë', 'r', 'n', 'â', 't', 'i', 'ô', 'n', 'à', 'l', 'i', 'z', 'æ',
                                    't', 'i', 'ø', 'n');

        assertTrue("Iñtërnâtiônàlizætiøn: " + toHexString(expected) + " vs " + toHexString(actual),
                Arrays.equals(expected, actual));
    }

    @Test
    public void testReadBinaryLength() {
        TreeSet<Long> tests = new TreeSet<>(Arrays.asList(0L, 1L, 2L, 51L, 250L, 251L, 2512L, 5828342L, 581759431825L));
        Random rnd = new Random();
        for (int a = 0; a < 100; a++)
            tests.add(rnd.nextLong());

        for (long value : tests) {
            // test plain read
            ByteBuf buf1 = Unpooled.buffer();
            writeLength(value, buf1);
            long actual = readBinaryLength(buf1);
            assertEquals(value, actual);
            assertEquals(0, buf1.readableBytes());

            // test read with first byte pre-read
            ByteBuf buf2 = Unpooled.buffer();
            writeLength(value, buf2);
            int firstByte = buf2.readUnsignedByte();

            long actual2 = readBinaryLength(firstByte, buf2);

            assertEquals(value, actual2);
            assertEquals(0, buf2.readableBytes());

            // test bytes before and after are left untouched
            ByteBuf buf3 = Unpooled.buffer();
            int prefix = rnd.nextInt(100);
            int suffix = rnd.nextInt(100);

            for (int a = 0; a < prefix; a++)
                buf3.writeByte(rnd.nextInt());

            writeLength(value, buf3);

            for (int a = 0; a < suffix; a++)
                buf3.writeByte(rnd.nextInt());

            buf3.skipBytes(prefix);
            long actual3 = readBinaryLength(buf3);
            assertEquals(value, actual3);
            assertEquals(suffix, buf3.readableBytes());
        }
    }

    @Test
    public void testReadBinaryLengthSpecialCases() {
        // 251 = GET_MORE_CLIENT_DATA
        // 255 = EOF

        assertEquals(-1, readBinaryLength(Unpooled.wrappedBuffer(bytes(251))));
        assertEquals(-2, readBinaryLength(Unpooled.wrappedBuffer(bytes(255))));

        // let's just check that all other values are non-special
        for (int first = 0; first <= 255; first++) {
            ByteBuf buf =  Unpooled.wrappedBuffer(bytes(first, 0, 0, 0, 0, 0, 0, 0, 0));
            long readBack = readBinaryLength(buf);
            if (first == 251 || first == 255) {
                assertTrue(readBack < 0);
            } else {
                assertTrue(readBack >= 0);
            }
        }
    }

    @Test
    public void testReadLengthEncodedString() {
        Random rnd = new Random();
        int[] byteLengths = new int[] {
            0, 1, 22, 240, 250, 251, 3661, 100000 + rnd.nextInt(100000), 1000000 + rnd.nextInt(1000000)
        };

        for (int targetLen : byteLengths) {
            StringBuilder sb = new StringBuilder();
            int bytes = 0;

            while (bytes < targetLen) {
                int cp = rnd.nextInt(0x110000);
                if (Character.isDefined(cp) && (cp >= 0x10000 || !Character.isSurrogate((char)cp))) {
                    int charLen;
                    if (cp <= 0x7F) {
                        charLen = 1;
                    } else
                    if (cp <= 0x7FF) {
                        charLen = 2;
                    } else
                    if (cp <= 0xFFFF) {
                        charLen = 3;
                    } else {
                        charLen = 4;
                    }

                    if (bytes + charLen <= targetLen) {
                        sb.appendCodePoint(cp);
                        bytes += charLen;
                    }
                }
            }

            ByteBuf buf = Unpooled.buffer();
            String expected = sb.toString();
            writeLengthEncodedString(expected, UTF_8, buf);
            String actual = readLengthEncodedString(buf, UTF_8);

            assertEquals(expected, actual);
        }

        // let's also do ISO_8859_1, to make sure charset is taken into account

        for (int targetLen : byteLengths) {
            StringBuilder sb = new StringBuilder();

            while (sb.length() < targetLen)
                sb.appendCodePoint(rnd.nextInt(256));

            ByteBuf buf = Unpooled.buffer();
            String expected = sb.toString();
            writeLengthEncodedString(expected, ISO_8859_1, buf);
            String actual = readLengthEncodedString(buf, ISO_8859_1);

            assertEquals(expected, actual);
        }
    }
}