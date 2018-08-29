package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.ServerMessage;
import io.netty.buffer.ByteBuf;

public interface MessageDecoder {
    ServerMessage decode(ByteBuf buffer);
}
