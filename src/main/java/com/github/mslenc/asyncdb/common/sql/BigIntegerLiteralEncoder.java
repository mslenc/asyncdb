package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

public class BigIntegerLiteralEncoder implements SqlLiteralEncoder {
    private static final BigIntegerLiteralEncoder instance = new BigIntegerLiteralEncoder();

    public static BigIntegerLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        ByteBufUtils.appendAsciiString(value.toString(), out);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(BigInteger.class);
    }
}
