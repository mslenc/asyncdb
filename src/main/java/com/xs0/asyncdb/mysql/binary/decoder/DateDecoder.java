package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
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
