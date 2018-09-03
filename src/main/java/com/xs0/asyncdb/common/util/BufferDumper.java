package com.xs0.asyncdb.common.util;

import io.netty.buffer.ByteBuf;

public class BufferDumper {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static final int BYTES_PER_ROW = 8;

    public static String dumpAsHex(ByteBuf buffer) {
        int numBytes = buffer.readableBytes();

        int firstPos = buffer.readerIndex();
        int veryLastPos = firstPos + numBytes;

        int rows = (numBytes + (BYTES_PER_ROW - 1)) / BYTES_PER_ROW;
        int rowNumLen = Integer.toString(rows - 1).length();

        StringBuilder sb = new StringBuilder((5 * BYTES_PER_ROW + rowNumLen + 7) * rows + 30);

        int rowBeginPos = firstPos;

        for (int row = 0; row < rows; row++, rowBeginPos += BYTES_PER_ROW) {
            int lastPosInRow = rowBeginPos + BYTES_PER_ROW;

            String rowNum = Integer.toString(row);

            for (int i = rowNum.length(); i < rowNumLen; i++)
                sb.append(' ');
            sb.append(rowNum);

            sb.append(':');

            for (int pos = rowBeginPos; pos < lastPosInRow; pos++) {
                if (pos < veryLastPos) {
                    int b = buffer.getByte(pos) & 255;
                    sb.append(' ');
                    sb.append(HEX_CHARS[b >>> 4]);
                    sb.append(HEX_CHARS[b & 15]);
                } else {
                    sb.append("   ");
                }
            }

            sb.append("    ");

            for (int pos = rowBeginPos; pos < lastPosInRow; pos++) {
                if (pos < veryLastPos) {
                    int b = buffer.getByte(pos) & 255;
                    if (b >= 32 && b < 127) {
                        sb.append((char) b);
                        sb.append(' ');
                    } else {
                        sb.append(". ");
                    }
                } else {
                    sb.append("  ");
                }
            }

            sb.append("\n");
        }

        sb.append("Total length = ").append(numBytes).append("\n");

        return sb.toString();
    }
}