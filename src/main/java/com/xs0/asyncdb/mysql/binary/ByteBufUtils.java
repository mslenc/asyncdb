package com.xs0.asyncdb.mysql.binary;

import com.xs0.asyncdb.mysql.ex.UnknownLengthException;
import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;
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
                    return read3ByteInt(buffer);
                case 254:
                    return buffer.readLongLE();
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
            buffer.writeShortLE((int)length);
        } else
        if (length < 16777216L) {
            buffer.writeByte(253);
            write3ByteInt((int)length, buffer);
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

    public static void writePacketLength(ByteBuf buffer) {
        writePacketLength(buffer, 1);
    }

    public static void writePacketLength(ByteBuf buffer, int sequence) {
        int length = buffer.writerIndex() - 4;
        buffer.markWriterIndex();
        buffer.writerIndex(0);

        write3BytesInt( buffer, length );
        buffer.writeByte(sequence);

        buffer.resetWriterIndex();
    }

    public static void write3BytesInt(ByteBuf b, int value) {
        b.writeByte(value);
        b.writeByte(value >>> 8);
        b.writeByte(value >>> 16);
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
}
