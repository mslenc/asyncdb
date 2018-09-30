package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

public class IntegralNumberLiteralEncoder implements SqlLiteralEncoder {
    private static final IntegralNumberLiteralEncoder instance = new IntegralNumberLiteralEncoder();

    public static IntegralNumberLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        ByteBufUtils.appendAsciiInteger(((Number)value).longValue(), out);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        HashSet<Class<?>> result = new HashSet<>();

        result.add(Byte.class);
        result.add(Short.class);
        result.add(Integer.class);
        result.add(Long.class);

        return result;
    }
}
