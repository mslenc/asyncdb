package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
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
