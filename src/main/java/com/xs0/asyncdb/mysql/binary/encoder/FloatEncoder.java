package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class FloatEncoder implements BinaryEncoder {
    @Override
    public void encode(Object value, ByteBuf buffer) {
        buffer.writeFloat((Float)value);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_FLOAT;
    }
}
