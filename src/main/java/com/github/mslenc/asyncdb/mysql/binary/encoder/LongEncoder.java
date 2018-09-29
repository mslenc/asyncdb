package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class LongEncoder implements BinaryEncoder {
    private static final LongEncoder instance = new LongEncoder();

    public static LongEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        buffer.writeLongLE((Long)value);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_LONGLONG;
    }
}