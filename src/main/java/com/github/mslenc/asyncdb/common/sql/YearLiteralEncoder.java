package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.Year;
import java.util.Collections;
import java.util.Set;

public class YearLiteralEncoder implements SqlLiteralEncoder {
    private static final YearLiteralEncoder instance = new YearLiteralEncoder();

    public static YearLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        ByteBufUtils.appendAsciiInteger(((Year)value).getValue(), out);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(Year.class);
    }
}
