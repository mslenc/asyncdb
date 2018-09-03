package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
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
