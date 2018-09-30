package com.github.mslenc.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

public abstract class ClientMessage {
    public abstract ByteBuf getPacketContents();

    public int getFirstPacketSequenceNumber() {
        return 0;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public abstract String toString(boolean fullDetails);
}
