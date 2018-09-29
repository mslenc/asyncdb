package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
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
            System.err.println("Yes, UNSIGNED");
            return Year.of(buffer.readUnsignedShortLE());
        } else {
            System.err.println("No, it is not UNSIGNED");
            return Year.of(buffer.readShortLE());
        }
    }
}
