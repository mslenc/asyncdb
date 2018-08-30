package com.xs0.asyncdb.mysql.binary.encoder;

import io.netty.buffer.ByteBuf;

public interface BinaryEncoder {
    void encode(Object value, ByteBuf buffer);
    int encodesTo();
}
