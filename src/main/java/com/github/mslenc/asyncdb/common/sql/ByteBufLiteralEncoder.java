package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.Set;

public class ByteBufLiteralEncoder implements SqlLiteralEncoder {
    private static final ByteBufLiteralEncoder instance = new ByteBufLiteralEncoder();

    public static ByteBufLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        ByteBuf bytes = (ByteBuf) value;

        out.writeBytes(ByteArrayLiteralEncoder.PROLOG);

        for (int remain = bytes.readableBytes(); remain > 0; remain--) {
            byte b = bytes.readByte();
            ByteBufUtils.writeByteAsHexPair(b, out);
        }

        out.writeBytes(ByteArrayLiteralEncoder.EPILOG);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(ByteBuf.class);
    }
}
