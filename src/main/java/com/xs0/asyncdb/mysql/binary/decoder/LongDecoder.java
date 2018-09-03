package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
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
