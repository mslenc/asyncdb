package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class IntegerEncoder implements BinaryEncoder {
    private static final IntegerEncoder instance = new IntegerEncoder();

    public static IntegerEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        buffer.writeInt((Integer)value);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_LONG;
    }
}
