package com.xs0.asyncdb.mysql.encoder;

import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newPacketBuffer;

public class QuitMessageEncoder implements MessageEncoder {
    private static final QuitMessageEncoder instance = new QuitMessageEncoder();

    public static QuitMessageEncoder instance() {
        return instance;
    }

    public ByteBuf encode(ClientMessage message) {
        ByteBuf buffer = newPacketBuffer(5);
        buffer.writeByte(ClientMessage.QUIT);
        return buffer;
    }
}

