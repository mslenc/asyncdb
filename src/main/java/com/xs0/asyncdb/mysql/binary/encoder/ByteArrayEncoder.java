package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class ByteArrayEncoder implements BinaryEncoder {
    private static final ByteArrayEncoder instance = new ByteArrayEncoder();

    public static ByteArrayEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        byte[] bytes = (byte[]) value;

        ByteBufUtils.writeLength(bytes.length, buffer);
        buffer.writeBytes(bytes);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_BLOB;
    }
}
