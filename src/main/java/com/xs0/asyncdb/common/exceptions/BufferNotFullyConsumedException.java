package com.xs0.asyncdb.common.exceptions;

import io.netty.buffer.ByteBuf;

public class BufferNotFullyConsumedException extends DatabaseException {
    public BufferNotFullyConsumedException(ByteBuf buffer) {
        super("Buffer was not fully consumed by decoder, " + buffer.readableBytes() + " bytes remained");
    }
}
