package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.writeLength;

public class ByteBufferEncoder implements BinaryEncoder {
    @Override
    public void encode(Object value, ByteBuf buffer) {
        ByteBuffer bytes = (ByteBuffer) value;

        writeLength(bytes.remaining(), buffer);
        buffer.writeBytes(bytes);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_BLOB;
    }
}
