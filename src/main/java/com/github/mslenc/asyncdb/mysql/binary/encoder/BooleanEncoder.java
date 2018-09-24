package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class BooleanEncoder implements BinaryEncoder {
    private static final BooleanEncoder instance = new BooleanEncoder();

    public static BooleanEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        if ((Boolean)value) {
            buffer.writeByte(1);
        } else {
            buffer.writeByte(0);
        }
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TINY;
    }
}
