package com.github.mslenc.asyncdb.mysql.column;

import io.netty.buffer.ByteBuf;

class TextValueDecoderUtils {
    static int readBytesIntoInt(ByteBuf packet, int byteLength) {
        int remain = byteLength;
        if (remain < 1)
            return 0;

        boolean negative = false;
        if (packet.getByte(packet.readerIndex()) == '-') {
            negative = true;
            packet.readByte();
            remain--;
        }

        int value = 0;
        while (remain-- > 0)
            value = value * 10 - (packet.readByte() - '0'); // accumulate negatively, to be able to read Integer.MIN_VALUE

        return negative ? value : -value;
    }

    static long readBytesIntoLong(ByteBuf packet, int byteLength) {
        if (byteLength <= 9)
            return readBytesIntoInt(packet, byteLength);

        int remain = byteLength;

        boolean negative = false;
        if (packet.getByte(packet.readerIndex()) == '-') {
            negative = true;
            packet.readByte();
            remain--;
        }

        long value = 0;
        while (remain-- > 0)
            value = value * 10 - (packet.readByte() - '0');

        return negative ? value : -value;
    }

    static String readKnownASCII(ByteBuf packet, int byteLength) {
        if (byteLength == 0)
            return "";

        StringBuilder sb = new StringBuilder(byteLength);
        for (int a = 0; a < byteLength; a++)
            sb.append((char)packet.readUnsignedByte());
        return sb.toString();
    }
}
