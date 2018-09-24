package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class ByteBufferEncoder implements BinaryEncoder {
    private static final ByteBufferEncoder instance = new ByteBufferEncoder();

    public static ByteBufferEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        ByteBuffer bytes = (ByteBuffer) value;

        ByteBufUtils.writeLength(bytes.remaining(), buffer);
        buffer.writeBytes(bytes);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_BLOB;
    }
}
