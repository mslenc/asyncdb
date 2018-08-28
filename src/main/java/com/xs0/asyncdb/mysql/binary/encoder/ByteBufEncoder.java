package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class ByteBufEncoder implements BinaryEncoder {
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
