package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class ByteTextDecoder implements TextValueDecoder {
    private static final ByteTextDecoder instance = new ByteTextDecoder();

    public static ByteTextDecoder instance() {
        return instance;
    }

    @Override
    public Byte decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        return (byte) TextValueDecoderUtils.readBytesIntoInt(packet, byteLength);
    }
}
