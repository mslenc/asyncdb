package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
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
