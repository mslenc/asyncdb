package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
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
