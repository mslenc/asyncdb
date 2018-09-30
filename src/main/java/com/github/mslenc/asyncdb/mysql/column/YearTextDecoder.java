package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.Year;

public class YearTextDecoder implements TextValueDecoder {
    private static final YearTextDecoder instance = new YearTextDecoder();

    public static YearTextDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        int year = TextValueDecoderUtils.readBytesIntoInt(packet, byteLength);
        return Year.of(year);
    }
}