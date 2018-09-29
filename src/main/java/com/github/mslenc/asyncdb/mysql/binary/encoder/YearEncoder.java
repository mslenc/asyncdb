package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.Year;

public class YearEncoder implements BinaryEncoder {
    private static final YearEncoder instance = new YearEncoder();

    public static YearEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        Year year = (Year) value;
        buffer.writeShortLE(year.getValue());
    }

    @Override
    public int encodesTo() {
        // for reasons not entirely understood at this time, using FIELD_TYPE_YEAR
        // causes a weird error to happen ( Error 1366 - HY000 - Incorrect integer value: '' for column 'the_value' at row 1)
        return ColumnType.FIELD_TYPE_SHORT;
    }
}
