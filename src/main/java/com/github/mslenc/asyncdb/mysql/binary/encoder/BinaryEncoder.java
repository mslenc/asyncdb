package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public interface BinaryEncoder {
    void encode(Object value, ByteBuf buffer, CodecSettings codecSettings);
    int encodesTo();
}
