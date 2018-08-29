package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class ByteBufEncoder implements BinaryEncoder {
    private static final ByteBufEncoder instance = new ByteBufEncoder();

    public static ByteBufEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        ByteBuf bytes = (ByteBuf) value;

        ByteBufUtils.writeLength(bytes.readableBytes(), buffer);
        buffer.writeBytes(bytes);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_BLOB;
    }
}
