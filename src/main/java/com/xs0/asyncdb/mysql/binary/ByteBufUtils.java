package com.xs0.asyncdb.mysql.binary;

import com.xs0.asyncdb.mysql.ex.UnknownLengthException;
import io.netty.buffer.ByteBuf;
import sun.security.util.Length;

import java.nio.charset.Charset;

public class ByteBufUtils {
    public static final int MYSQL_NULL = 0xfb;

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
        int firstByte = buffer.readUnsignedByte();

        if (firstByte <= 250) {
            return firstByte;
        } else {
            switch (firstByte) {
                case MYSQL_NULL:
                    return -1;
                case 252:
                    return buffer.readUnsignedShort();
                case 253:
                    return read3ByteInt(buffer);
                case 254:
                    return buffer.readLong();
                default:
                    throw new UnknownLengthException(firstByte);
            }
        }
    }

    public static int read3ByteInt(ByteBuf buffer) {
        int a = buffer.readUnsignedByte();
        int b = buffer.readUnsignedByte();
        int c = buffer.readUnsignedByte();

        return a | (b << 8) | (c << 16);
    }

    public static void write3ByteInt(int value, ByteBuf buffer) {
        buffer.writeByte(value);
        buffer.writeByte(value >>> 8);
        buffer.writeByte(value >>> 16);
    }

    public static void writeLength(long length, ByteBuf buffer) {
        if (length < 251L) {
            buffer.writeByte((int)length);
        } else
        if (length < 65536L) {
            buffer.writeByte(252);
            buffer.writeShort((int)length);
        } else
        if (length < 16777216L) {
            buffer.writeByte(253);
            write3ByteInt((int)length, buffer);
        } else {
            buffer.writeByte(254);
            buffer.writeLong(length);
        }
    }

    public static void writeLengthEncodedString(String string, Charset charset, ByteBuf buffer) {
        byte[] bytes = string.getBytes(charset);
        writeLength(bytes.length, buffer);
        buffer.writeBytes(bytes);
    }
}
