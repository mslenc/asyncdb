package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

public class OtherNumberLiteralEncoder implements SqlLiteralEncoder {
    private static final OtherNumberLiteralEncoder instance = new OtherNumberLiteralEncoder();

    public static OtherNumberLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        ByteBufUtils.appendAsciiString(value.toString(), out);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        HashSet<Class<?>> result = new HashSet<>();

        result.add(Float.class);
        result.add(Double.class);
        result.add(Number.class);

        return result;
    }
}
