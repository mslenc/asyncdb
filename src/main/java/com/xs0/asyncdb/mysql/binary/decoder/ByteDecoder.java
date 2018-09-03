package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class ByteDecoder implements BinaryDecoder {
    private static final ByteDecoder instance = new ByteDecoder();

    public static ByteDecoder instance() {
        return instance;
    }

    @Override
    public Number decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        if (columnData.isUnsigned()) {
            return buffer.readUnsignedByte();
        } else {
            return buffer.readByte();
        }
    }
}
