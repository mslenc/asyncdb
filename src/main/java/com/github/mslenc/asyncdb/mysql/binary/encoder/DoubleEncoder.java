package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class DoubleEncoder implements BinaryEncoder {
    private static final DoubleEncoder instance = new DoubleEncoder();

    public static DoubleEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        buffer.writeDoubleLE((Double)value);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_DOUBLE;
    }
}
