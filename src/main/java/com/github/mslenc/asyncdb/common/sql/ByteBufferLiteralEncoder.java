package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;

public class ByteBufferLiteralEncoder implements SqlLiteralEncoder {
    private static final ByteBufferLiteralEncoder instance = new ByteBufferLiteralEncoder();

    public static ByteBufferLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        ByteBuffer bytes = (ByteBuffer) value;

        out.writeBytes(ByteArrayLiteralEncoder.PROLOG);

        for (int remain = bytes.remaining(); remain > 0; remain--) {
            byte b = bytes.get();
            ByteBufUtils.writeByteAsHexPair(b, out);
        }

        out.writeBytes(ByteArrayLiteralEncoder.EPILOG);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(ByteBuffer.class);
    }
}
