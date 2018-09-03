package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class ShortDecoder implements BinaryDecoder {
    private static final ShortDecoder instance = new ShortDecoder();

    public static ShortDecoder instance() {
        return instance;
    }

    @Override
    public Number decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        if (columnData.isUnsigned()) {
            return buffer.readUnsignedShortLE();
        } else {
            return buffer.readShortLE();
        }
    }
}
