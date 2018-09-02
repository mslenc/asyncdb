package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public class LongDecoder implements BinaryDecoder {
    private static final LongDecoder instance = new LongDecoder();

    public static LongDecoder instance() {
        return instance;
    }

    @Override
    public Long decode(ByteBuf buffer) {
        return buffer.readLongLE();
    }
}
