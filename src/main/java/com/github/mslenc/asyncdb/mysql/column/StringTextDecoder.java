package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public class StringTextDecoder implements TextValueDecoder {
    private static final StringTextDecoder instance = new StringTextDecoder();

    public static StringTextDecoder instance() {
        return instance;
    }

    @Override
    public String decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        return packet.readCharSequence(byteLength, codecSettings.charset()).toString();
    }
}
