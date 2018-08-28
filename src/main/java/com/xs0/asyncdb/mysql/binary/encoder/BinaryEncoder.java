package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public interface BinaryEncoder {
    void encode(Object value, ByteBuf buffer);
    ColumnType encodesTo();
}
