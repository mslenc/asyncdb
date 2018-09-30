package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public interface TextValueDecoder {
    default Object decode(ColumnData kind, ByteBuf value, Charset charset) {
        byte[] bytes = new byte[value.readableBytes()];
        value.readBytes(bytes);
        return decode(new String(bytes, charset));
    }

    Object decode(String value);
}
