package com.xs0.asyncdb.mysql.binary;

import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class ByteBufUtils {


    public static String readLengthEncodedString(ByteBuf buffer, Charset charset) {
        int length = (int) readBinaryLength(buffer);
        return readFixedString(buffer, length, charset);
    }

    public static String readFixedString(ByteBuf buffer, int length, Charset charset) {
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, charset);
    }

    public static long readBinaryLength(ByteBuf buffer) {
        return readBinaryLength(buffer.readUnsignedByte(), buffer);
    }

    public static long readBinaryLength(int firstUnsignedByte, ByteBuf buffer) {
        if (firstUnsignedByte <= 250) {
            return firstUnsignedByte;
        } else {
            switch (firstUnsignedByte) {
                case MySQLIO.PACKET_HEADER_GET_MORE_CLIENT_DATA:
                    return -1;
                case MySQLIO.PACKET_HEADER_ERR:
                    return -2;

                case 252:
                    return buffer.readUnsignedShortLE();
                case 253:
                    return buffer.readUnsignedMediumLE();
                case 254:
                    return buffer.readLongLE();
            }
        }

        throw new AssertionError("Unreachable");
    }

    public static void writeLength(long length, ByteBuf buffer) {
        switch (Long.numberOfLeadingZeros(length)) {
            case 64:
            case 63: case 62: case 61: case 60: case 59: case 58: case 57: case 56:
                if (length <= 250) {
                    buffer.writeByte((int) length);
                    break;
                }
                // else fallthrough

            case 55: case 54: case 53: case 52: case 51: case 50: case 49: case 48:
                buffer.writeByte(252);
                buffer.writeShortLE((int)length);
                break;

            case 47: case 46: case 45: case 44: case 43: case 42: case 41: case 40:
                buffer.writeByte(253);
                buffer.writeMediumLE((int)length);
                break;

            default:
                buffer.writeByte(254);
                buffer.writeLongLE(length);
                break;
        }
    }

    public static void writeLengthEncodedString(String string, Charset charset, ByteBuf buffer) {
        byte[] bytes = string.getBytes(charset);
        writeLength(bytes.length, buffer);
        buffer.writeBytes(bytes);
    }

    public static String readCString(ByteBuf b, Charset charset) {
        int count = b.bytesBefore((byte)0);
        if (count < 0)
            return null;

        String result = b.toString(b.readerIndex(), count, charset);
        b.readerIndex(b.readerIndex() + count + 1);

        return result;
    }

    public static String readUntilEOF(ByteBuf b, Charset charset) {
        String result = b.toString(charset);
        b.readerIndex(b.readerIndex() + b.readableBytes());
        return result;
    }

    public static String readUntilEOFOrZero(ByteBuf b, Charset charset) {
        // This is specifically for authentication method in initial handshake, see
        // https://bugs.mysql.com/bug.php?id=59453

        int length = b.bytesBefore((byte)0);
        if (length < 0) // no zero found, so ..
            return readUntilEOF(b, charset);

        int readerIndex = b.readerIndex();

        String result = b.toString(readerIndex, length, charset);
        b.readerIndex(readerIndex + length + 1); // 1 is for NUL at the end
        return result;
    }

    public static void writeCString(ByteBuf b, String content, Charset charset) {
        b.writeCharSequence(content, charset);
        b.writeByte(0);
    }

    public static byte[] readFixedBytes(ByteBuf buffer, int length) {
        if (buffer.readableBytes() < length)
            return null;

        byte[] result = new byte[length];
        buffer.readBytes(result);
        return result;
    }

    public static void setNullBit(int offset, byte[] nullBytes, int index) {
        // https://dev.mysql.com/doc/internals/en/null-bitmap.html

        int pos = offset + index;
        nullBytes[pos >>> 3] |= 1 << (pos & 7);
    }

    public static boolean isNullBitSet(int offset, byte[] nullBytes, int index) {
        int pos = offset + index;

        return (nullBytes[pos >>> 3] & (1 << (pos & 7))) != 0;
    }

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public static String toHexString(byte[] bytes) {
        StringBuilder out = new StringBuilder();

        for (byte b : bytes) {
            out.append(HEX_CHARS[(b & 0xF0) >>> 4]);
            out.append(HEX_CHARS[b & 0x0F]);
        }

        return out.toString();
    }
}
