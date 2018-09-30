package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

public class BigDecimalLiteralEncoder implements SqlLiteralEncoder {
    private static final BigDecimalLiteralEncoder instance = new BigDecimalLiteralEncoder();

    public static BigDecimalLiteralEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf out, CodecSettings settings) {
        // we don't want E (exponent), to keep the value exact (MySQL treats numbers with E as floating-points)
        ByteBufUtils.appendAsciiString(((BigDecimal)value).toPlainString(), out);
    }

    @Override
    public Set<Class<?>> supportedClasses() {
        return Collections.singleton(BigDecimal.class);
    }
}
