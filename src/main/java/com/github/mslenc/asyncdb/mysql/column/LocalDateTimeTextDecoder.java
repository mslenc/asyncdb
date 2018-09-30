package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalDateTimeTextDecoder implements TextValueDecoder {
    private static final LocalDateTimeTextDecoder instance = new LocalDateTimeTextDecoder();

    public static LocalDateTimeTextDecoder instance() {
        return instance;
    }

    private DateTimeFormatter format =
        new DateTimeFormatterBuilder().
            appendPattern("yyyy-MM-dd HH:mm:ss").
            optionalStart().
            appendPattern(".SSSSSS").
            toFormatter();

    @Override
    public LocalDateTime decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        String str = TextValueDecoderUtils.readKnownASCII(packet, byteLength);

        if (str.startsWith("0000-00-00 00:00:00")) {
            return null;
        } else {
            return LocalDateTime.parse(str, format);
        }
    }
}
