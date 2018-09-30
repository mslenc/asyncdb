package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class ShortTextDecoder implements TextValueDecoder {
    private static final ShortTextDecoder instance = new ShortTextDecoder();

    public static ShortTextDecoder instance() {
        return instance;
    }

    @Override
    public Short decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        return (short) TextValueDecoderUtils.readBytesIntoInt(packet, byteLength);
    }
}