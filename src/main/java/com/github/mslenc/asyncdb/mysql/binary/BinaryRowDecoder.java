package com.github.mslenc.asyncdb.mysql.binary;

import com.github.mslenc.asyncdb.common.exceptions.BufferNotFullyConsumedException;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class BinaryRowDecoder {
    private static final int NULL_MASK_OFFSET = 2;

    private static final BinaryRowDecoder instance = new BinaryRowDecoder();

    public static BinaryRowDecoder instance() {
        return instance;
    }

    public Object[] decode(ByteBuf buffer, List<ColumnDefinitionMessage> columns, CodecSettings settings) {
        buffer.readByte(); // header

        int nullCount = (columns.size() + 7 + NULL_MASK_OFFSET) / 8;

        byte[] nullBytes = new byte[nullCount];
        buffer.readBytes(nullBytes);

        Object[] row = new Object[columns.size()];

        for (int i = 0, n = columns.size(); i < n; i++) {
            if (ByteBufUtils.isNullBitSet(NULL_MASK_OFFSET, nullBytes, i))
                continue;

            ColumnDefinitionMessage column = columns.get(i);

            row[i] = column.binaryDecoder.decode(buffer, settings, column);
        }

        if (buffer.readableBytes() != 0) {
            throw new BufferNotFullyConsumedException(buffer);
        }

        return row;
    }
}