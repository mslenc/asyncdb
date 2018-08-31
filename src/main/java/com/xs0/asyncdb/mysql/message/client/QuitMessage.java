package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

public class QuitMessage implements ClientMessage {
    private static final QuitMessage instance = new QuitMessage();

    public static QuitMessage instance() {
        return instance;
    }

    public int kind() {
        return QUIT;
    }

    @Override
    public void encodeInto(ByteBuf packet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int packetSequenceNumber() {
        return 0;
    }
}
