package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class ByteArrayTextDecoder implements TextValueDecoder {
    private static final ByteArrayTextDecoder instance = new ByteArrayTextDecoder();

    public static ByteArrayTextDecoder instance() {
        return instance;
    }

    @Override
    public byte[] decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        byte[] bytes = new byte[byteLength];
        packet.readBytes(bytes);
        return bytes;
    }
}
