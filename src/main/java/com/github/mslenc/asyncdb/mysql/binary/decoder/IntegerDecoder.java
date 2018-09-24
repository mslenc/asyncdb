package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

public class IntegerDecoder implements BinaryDecoder {
    private static final IntegerDecoder instance = new IntegerDecoder();

    public static IntegerDecoder instance() {
        return instance;
    }

    @Override
    public Number decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        if (columnData.isUnsigned()) {
            return buffer.readUnsignedIntLE();
        } else {
            return buffer.readIntLE();
        }
    }
}
