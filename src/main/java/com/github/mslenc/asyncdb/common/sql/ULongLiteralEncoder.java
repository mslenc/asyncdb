package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.common.ULong;
import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.Set;

public class ULongLiteralEncoder implements SqlLiteralEncoder {
    private static final ULongLiteralEncoder instance = new ULongLiteralEncoder();

    public static ULongLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        long longVal = ((ULong)value).longValue();

        if (longVal >= 0) {
            ByteBufUtils.appendAsciiInteger(longVal, out);
        } else {
            ByteBufUtils.appendAsciiString(value.toString(), out);
        }
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(ULong.class);
    }
}
