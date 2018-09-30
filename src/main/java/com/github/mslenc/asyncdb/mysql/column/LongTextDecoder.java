package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class LongTextDecoder implements TextValueDecoder {
    private static final LongTextDecoder instance = new LongTextDecoder();

    public static LongTextDecoder instance() {
        return instance;
    }

    @Override
    public Long decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        return TextValueDecoderUtils.readBytesIntoLong(packet, byteLength);
    }
}