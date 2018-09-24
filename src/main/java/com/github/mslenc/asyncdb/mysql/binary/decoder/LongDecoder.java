package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

public class LongDecoder implements BinaryDecoder {
    private static final LongDecoder instance = new LongDecoder();

    public static LongDecoder instance() {
        return instance;
    }

    @Override
    public Long decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        return buffer.readLongLE();
    }
}
