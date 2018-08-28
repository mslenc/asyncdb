package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public class ByteDecoder implements BinaryDecoder {
    private static final ByteDecoder instance = new ByteDecoder();

    public static ByteDecoder instance() {
        return instance;
    }

    @Override
    public Byte decode(ByteBuf buffer) {
        return buffer.readByte();
    }
}
