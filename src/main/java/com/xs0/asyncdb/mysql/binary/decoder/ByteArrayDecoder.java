package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public class ByteArrayDecoder implements BinaryDecoder {
    private static final ByteArrayDecoder instance = new ByteArrayDecoder();

    public static ByteArrayDecoder instance() {
        return instance;
    }

    @Override
    public byte[] decode(ByteBuf buffer) {
        int length = (int)ByteBufUtils.readBinaryLength(buffer);
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return bytes;
    }
}
