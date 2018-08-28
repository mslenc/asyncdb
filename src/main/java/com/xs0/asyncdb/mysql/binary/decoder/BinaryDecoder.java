package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public interface BinaryDecoder {
    Object decode(ByteBuf buffer);
}
