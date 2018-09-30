package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.ULong;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class ULongTextDecoder implements TextValueDecoder {
    private static final ULongTextDecoder instance = new ULongTextDecoder();

    public static ULongTextDecoder instance() {
        return instance;
    }

    @Override
    public ULong decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        String str = TextValueDecoderUtils.readKnownASCII(packet, byteLength);
        return ULong.valueOf(str);
    }
}