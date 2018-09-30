package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class IntegerTextDecoder implements TextValueDecoder {
    private static final IntegerTextDecoder instance = new IntegerTextDecoder();

    public static IntegerTextDecoder instance() {
        return instance;
    }

    @Override
    public Integer decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        return TextValueDecoderUtils.readBytesIntoInt(packet, byteLength);
    }
}
