package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;

public class BigDecimalTextDecoder implements TextValueDecoder {
    private static final BigDecimalTextDecoder instance = new BigDecimalTextDecoder();

    public static BigDecimalTextDecoder instance() {
        return instance;
    }

    @Override
    public BigDecimal decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        String str = TextValueDecoderUtils.readKnownASCII(packet, byteLength);
        return new BigDecimal(str);
    }
}
