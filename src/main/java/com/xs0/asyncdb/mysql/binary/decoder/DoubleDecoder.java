package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public class DoubleDecoder implements BinaryDecoder {
    private static final DoubleDecoder instance = new DoubleDecoder();

    public static DoubleDecoder instance() {
        return instance;
    }

    @Override
    public Double decode(ByteBuf buffer) {
        return buffer.readDouble();
    }
}
