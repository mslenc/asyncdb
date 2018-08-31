package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public class IntegerDecoder implements BinaryDecoder {
    private static final IntegerDecoder instance = new IntegerDecoder();

    public static IntegerDecoder instance() {
        return instance;
    }

    @Override
    public Integer decode(ByteBuf buffer) {
        return buffer.readIntLE();
    }
}
