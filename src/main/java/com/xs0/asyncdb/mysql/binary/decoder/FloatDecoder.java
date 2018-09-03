package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class FloatDecoder implements BinaryDecoder {
    private static final FloatDecoder instance = new FloatDecoder();

    public static FloatDecoder instance() {
        return instance;
    }

    @Override
    public Float decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        return buffer.readFloatLE();
    }
}
