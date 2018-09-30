package com.github.mslenc.asyncdb.common.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public interface ColumnDecoder {
    default Object decode(ColumnData kind, ByteBuf value, Charset charset) {
        byte[] bytes = new byte[value.readableBytes()];
        value.readBytes(bytes);
        return decode(new String(bytes, charset));
    }

    Object decode(String value);
}
