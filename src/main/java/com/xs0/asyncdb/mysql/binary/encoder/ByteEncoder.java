package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class ByteEncoder implements BinaryEncoder {
    private static final ByteEncoder instance = new ByteEncoder();

    public static ByteEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        buffer.writeByte((Byte)value);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TINY;
    }
}
