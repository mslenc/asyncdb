package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class DoubleTextDecoder implements TextValueDecoder {
    private static final DoubleTextDecoder instance = new DoubleTextDecoder();

    public static DoubleTextDecoder instance() {
        return instance;
    }

    @Override
    public Double decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        String str = TextValueDecoderUtils.readKnownASCII(packet, byteLength);
        return Double.valueOf(str);
    }
}
