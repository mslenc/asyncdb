package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.Instant;
import java.time.LocalDateTime;

public class InstantDecoder implements BinaryDecoder {
    private static final InstantDecoder instance = new InstantDecoder();

    public static InstantDecoder instance() {
        return instance;
    }

    @Override
    public Instant decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        LocalDateTime value = DateTimeDecoder.instance().decode(buffer, settings, columnData);

        return value.atZone(settings.timezone()).toInstant();
    }
}
