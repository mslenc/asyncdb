package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

public class IntegerEncoder implements BinaryEncoder {
    private static final IntegerEncoder instance = new IntegerEncoder();

    public static IntegerEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        buffer.writeIntLE((Integer)value);
    }

    @Override
    public int encodesTo() {
        return MySQLIO.FIELD_TYPE_LONG;
    }
}
