package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class LongEncoder implements BinaryEncoder {
    private static final LongEncoder instance = new LongEncoder();

    public static LongEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        buffer.writeLongLE((Long)value);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_LONGLONG;
    }
}
