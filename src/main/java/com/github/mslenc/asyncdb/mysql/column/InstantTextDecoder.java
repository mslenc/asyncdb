package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class InstantTextDecoder implements TextValueDecoder {
    private static final InstantTextDecoder instance = new InstantTextDecoder();

    public static InstantTextDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        LocalDateTime ldt = LocalDateTimeTextDecoder.instance().decode(kind, packet, byteLength, codecSettings);
        if (ldt == null)
            return null;

        return ldt.atZone(ZoneOffset.UTC).toInstant();
    }
}
