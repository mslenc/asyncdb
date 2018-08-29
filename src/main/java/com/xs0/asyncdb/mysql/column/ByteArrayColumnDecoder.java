package com.xs0.asyncdb.mysql.column;

import com.xs0.asyncdb.common.column.ColumnDecoder;
import com.xs0.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

public class ByteArrayColumnDecoder implements ColumnDecoder {
    private static final ByteArrayColumnDecoder instance = new ByteArrayColumnDecoder();

    public static ByteArrayColumnDecoder instance() {
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
