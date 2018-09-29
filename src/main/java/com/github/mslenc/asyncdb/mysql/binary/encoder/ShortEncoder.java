package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

public class ShortEncoder implements BinaryEncoder {
    private static final ShortEncoder instance = new ShortEncoder();

    public static ShortEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        buffer.writeShortLE((Short)value);
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_SHORT;
    }
}
