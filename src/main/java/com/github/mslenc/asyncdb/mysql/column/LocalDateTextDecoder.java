package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.LocalDate;

public class LocalDateTextDecoder implements TextValueDecoder {
    private static final LocalDateTextDecoder instance = new LocalDateTextDecoder();

    public static LocalDateTextDecoder instance() {
        return instance;
    }

    @Override
    public LocalDate decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        String str = TextValueDecoderUtils.readKnownASCII(packet, byteLength);

        if (str.equals("0000-00-00")) {
            return null;
        } else {
            return LocalDate.parse(str);
        }
    }
}
