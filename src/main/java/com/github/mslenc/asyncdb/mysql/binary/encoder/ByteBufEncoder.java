package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

public class ByteBufEncoder implements BinaryEncoder {
    private static final ByteBufEncoder instance = new ByteBufEncoder();

    public static ByteBufEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer, CodecSettings codecSettings) {
        ByteBuf bytes = (ByteBuf) value;

        ByteBufUtils.writeLength(bytes.readableBytes(), buffer);
        buffer.writeBytes(bytes);
    }

    @Override
    public int encodesTo() {
        return MySQLIO.FIELD_TYPE_BLOB;
    }
}
