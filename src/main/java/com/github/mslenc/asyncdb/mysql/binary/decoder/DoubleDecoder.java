package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

public class DoubleDecoder implements BinaryDecoder {
    private static final DoubleDecoder instance = new DoubleDecoder();

    public static DoubleDecoder instance() {
        return instance;
    }

    @Override
    public Double decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        return buffer.readDoubleLE();
    }
}
