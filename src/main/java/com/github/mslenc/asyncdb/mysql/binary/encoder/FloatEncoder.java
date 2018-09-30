package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

public class FloatEncoder implements BinaryEncoder {
    private static final FloatEncoder instance = new FloatEncoder();

    public static FloatEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        buffer.writeFloatLE((Float)value);
    }

    @Override
    public int encodesTo() {
        return MySQLIO.FIELD_TYPE_FLOAT;
    }
}
