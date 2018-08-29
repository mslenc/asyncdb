package com.xs0.asyncdb.mysql.binary;

import com.xs0.asyncdb.mysql.ex.UnknownLengthException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;
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

    public static String readCString(ByteBuf b, Charset charset) {
        b.markReaderIndex();

        int count = b.bytesBefore((byte)0);
        String result = b.toString(b.readerIndex(), count, charset);
        b.readerIndex(b.readerIndex() + count + 1);

        return result;
    }

    public static String readUntilEOF(ByteBuf b, Charset charset) {
        if (b.readableBytes() == 0) {
            return "";
        }

        b.markReaderIndex();

        int readByte = -1;
        int count = 0;
        int offset = 1;

        while (readByte != 0) {
            if (b.readableBytes() > 0) {
                readByte = b.readByte();
                count += 1;
            } else {
                readByte = 0;
                offset = 0;
            }
        }

        b.resetReaderIndex();

        String result = b.toString(b.readerIndex(), count - offset, charset);

        b.readerIndex(b.readerIndex() + count);

        return result;
    }

    public static ByteBuf newPacketBuffer() {
        return newPacketBuffer(1024);
    }

    public static ByteBuf newPacketBuffer(int sizeEstimate) {
        ByteBuf buffer = newMysqlBuffer(sizeEstimate);
        buffer.writeInt(0);
        return buffer;
    }

    public static ByteBuf newMysqlBuffer() {
        return newMysqlBuffer(1024);
    }

    public static ByteBuf newMysqlBuffer(int sizeEstimate) {
        return Unpooled.buffer(sizeEstimate).order(ByteOrder.LITTLE_ENDIAN);
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
}
