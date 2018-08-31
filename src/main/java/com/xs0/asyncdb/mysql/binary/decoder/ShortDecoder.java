package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public class ShortDecoder implements BinaryDecoder {
    private static final ShortDecoder instance = new ShortDecoder();

    public static ShortDecoder instance() {
        return instance;
    }

    @Override
    public Short decode(ByteBuf buffer) {
        return buffer.readShortLE();
    }
}
