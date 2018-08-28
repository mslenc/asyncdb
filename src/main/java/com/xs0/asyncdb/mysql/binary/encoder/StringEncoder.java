package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class StringEncoder implements BinaryEncoder {
    private final Charset charset;

    StringEncoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        ByteBufUtils.writeLengthEncodedString(value.toString(), charset, buffer);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_VARCHAR;
    }
}
