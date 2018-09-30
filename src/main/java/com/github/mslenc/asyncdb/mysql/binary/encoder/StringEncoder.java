package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class StringEncoder implements BinaryEncoder {
    private final Charset charset;

    public StringEncoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        ByteBufUtils.writeLengthEncodedString(value.toString(), charset, buffer);
    }

    @Override
    public int encodesTo() {
        return MySQLIO.FIELD_TYPE_VARCHAR;
    }
}
