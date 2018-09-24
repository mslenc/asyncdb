package com.github.mslenc.asyncdb.mysql.decoder;

import com.github.mslenc.asyncdb.mysql.message.server.ServerMessage;
import io.netty.buffer.ByteBuf;

public interface MessageDecoder {
    ServerMessage decode(ByteBuf buffer);
}
