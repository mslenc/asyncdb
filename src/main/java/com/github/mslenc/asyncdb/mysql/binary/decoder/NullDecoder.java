package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

public class NullDecoder implements BinaryDecoder {
    private static final NullDecoder instance = new NullDecoder();

    public static NullDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        return null;
    }
}
