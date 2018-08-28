package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public class NullDecoder implements BinaryDecoder {
    private static final NullDecoder instance = new NullDecoder();

    public static NullDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(ByteBuf buffer) {
        return null;
    }
}
