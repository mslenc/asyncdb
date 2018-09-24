package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateDecoder implements BinaryDecoder {
    private static final DateDecoder instance = new DateDecoder();

    public static DateDecoder instance() {
        return instance;
    }

    @Override
    public LocalDate decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        LocalDateTime ts = DateTimeDecoder.instance().decode(buffer, settings, columnData);

        if (ts != null) {
            return ts.toLocalDate();
        } else {
            return null;
        }
    }
}
