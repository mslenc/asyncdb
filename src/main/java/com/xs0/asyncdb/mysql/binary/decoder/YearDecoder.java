package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.Year;

public class YearDecoder implements BinaryDecoder {
    private static final YearDecoder instance = new YearDecoder();

    public static YearDecoder instance() {
        return instance;
    }

    @Override
    public Year decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        if (columnData.isUnsigned()) {
            return Year.of(buffer.readUnsignedShortLE());
        } else {
            return Year.of(buffer.readShortLE());
        }
    }
}
