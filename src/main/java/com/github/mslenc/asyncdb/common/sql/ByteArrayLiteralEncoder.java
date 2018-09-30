package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public class ByteArrayLiteralEncoder implements SqlLiteralEncoder {
    private static final ByteArrayLiteralEncoder instance = new ByteArrayLiteralEncoder();

    public static ByteArrayLiteralEncoder instance() {
        return instance;
    }

    static final byte[] PROLOG = "x'".getBytes(StandardCharsets.UTF_8);
    static final byte[] EPILOG = "'".getBytes(StandardCharsets.UTF_8);

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        byte[] bytes = (byte[]) value;

        out.writeBytes(PROLOG);

        for (byte b : bytes)
            ByteBufUtils.writeByteAsHexPair(b, out);

        out.writeBytes(EPILOG);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(byte[].class);
    }
}
