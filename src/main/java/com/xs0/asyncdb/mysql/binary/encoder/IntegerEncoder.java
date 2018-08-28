package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class IntegerEncoder implements BinaryEncoder {
    @Override
    public void encode(Object value, ByteBuf buffer) {
        buffer.writeInt((Integer)value);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_LONG;
    }
}
