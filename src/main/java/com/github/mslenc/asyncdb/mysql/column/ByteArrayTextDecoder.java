package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

public class ByteArrayTextDecoder implements TextValueDecoder {
    private static final ByteArrayTextDecoder instance = new ByteArrayTextDecoder();

    public static ByteArrayTextDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(ColumnData kind, ByteBuf value, Charset charset) {
        byte[] bytes = new byte[value.readableBytes()];
        value.readBytes(bytes);
        return bytes;
    }

    @Override
    public Object decode(String value) {
        throw new UnsupportedOperationException("This method should never be called for byte arrays");
    }
}
