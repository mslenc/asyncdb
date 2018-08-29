package com.xs0.asyncdb.mysql.encoder;

import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import io.netty.buffer.ByteBuf;

public interface MessageEncoder {
    ByteBuf encode(ClientMessage message);
}
