package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class FloatTextDecoder implements TextValueDecoder {
    private static final FloatTextDecoder instance = new FloatTextDecoder();

    public static FloatTextDecoder instance() {
        return instance;
    }

    @Override
    public Float decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        String str = TextValueDecoderUtils.readKnownASCII(packet, byteLength);
        return Float.valueOf(str);
    }
}
