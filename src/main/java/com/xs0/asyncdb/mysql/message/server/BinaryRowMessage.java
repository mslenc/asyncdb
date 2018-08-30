package com.xs0.asyncdb.mysql.message.server;

import io.netty.buffer.ByteBuf;

public class BinaryRowMessage implements ServerMessage {
    public final ByteBuf buffer;

    public BinaryRowMessage(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public int kind() {
        return BINARY_ROW;
    }
}
