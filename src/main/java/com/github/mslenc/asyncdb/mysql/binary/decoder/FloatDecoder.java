package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
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
