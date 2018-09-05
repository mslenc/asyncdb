package com.xs0.asyncdb.mysql.binary;

import com.xs0.asyncdb.mysql.ex.UnknownLengthException;
import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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

    public static long readBinaryLength(int firstByte, ByteBuf buffer) {
        if (firstByte <= 250) {
            return firstByte;
        } else {
            switch (firstByte) {
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
                default:
                    throw new UnknownLengthException(firstByte);
            }
        }
    }

    public static void writeLength(long length, ByteBuf buffer) {
        if (length < 251L) {
            buffer.writeByte((int)length);
        } else
        if (length < 65536L) {
            buffer.writeByte(252);
            buffer.writeShortLE((int)length);
        } else
        if (length < 16777216L) {
            buffer.writeByte(253);
            buffer.writeMediumLE((int)length);
        } else {
            buffer.writeByte(254);
            buffer.writeLongLE(length);
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

    public static ByteBuf newMysqlBuffer() {
        return newMysqlBuffer(1024);
    }

    public static ByteBuf newMysqlBuffer(int sizeEstimate) {
        return Unpooled.buffer(sizeEstimate);
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
