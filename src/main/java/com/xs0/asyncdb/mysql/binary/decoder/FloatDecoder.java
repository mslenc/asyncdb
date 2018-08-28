package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

public class FloatDecoder implements BinaryDecoder {
    private static final FloatDecoder instance = new FloatDecoder();

    public static FloatDecoder instance() {
        return instance;
    }

    @Override
    public Float decode(ByteBuf buffer) {
        return buffer.readFloat();
    }
}
