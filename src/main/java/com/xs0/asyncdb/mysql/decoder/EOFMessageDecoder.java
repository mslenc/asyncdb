package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.EOFMessage;
import com.xs0.asyncdb.mysql.message.server.ServerMessage;
import io.netty.buffer.ByteBuf;

public class EOFMessageDecoder implements MessageDecoder {
    private static final EOFMessageDecoder instance = new EOFMessageDecoder();

    public static EOFMessageDecoder instance() {
        return instance;
    }

    @Override
    public EOFMessage decode(ByteBuf buffer) {
        return new EOFMessage(
            buffer.readUnsignedShort(),
            buffer.readUnsignedShort()
        );
    }
}
