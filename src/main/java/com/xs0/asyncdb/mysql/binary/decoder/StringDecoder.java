package com.xs0.asyncdb.mysql.binary.decoder;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readLengthEncodedString;

class StringDecoder implements BinaryDecoder {
    private final Charset charset;

    public StringDecoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String decode(ByteBuf buffer) {
        return readLengthEncodedString(buffer, charset);
    }
}