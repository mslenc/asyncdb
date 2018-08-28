package com.xs0.asyncdb.common.column;

import com.xs0.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public interface ColumnDecoder {
    default Object decode(ColumnData kind, ByteBuf value, Charset charset) {
        byte[] bytes = new byte[value.readableBytes()];
        value.readBytes(bytes);
        return decode(new String(bytes, charset));
    }

    Object decode(String value);

    default boolean supportsStringDecoding() {
        return true;
    }
}
