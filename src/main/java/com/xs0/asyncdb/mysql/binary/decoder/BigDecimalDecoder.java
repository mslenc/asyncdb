package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readLengthEncodedString;

class BigDecimalDecoder implements BinaryDecoder {
    private final Charset charset;

    public BigDecimalDecoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public BigDecimal decode(ByteBuf buffer) {
        return new BigDecimal(readLengthEncodedString(buffer, charset));
    }
}