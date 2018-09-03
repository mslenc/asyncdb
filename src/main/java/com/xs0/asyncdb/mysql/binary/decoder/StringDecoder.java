package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readLengthEncodedString;

public class StringDecoder implements BinaryDecoder {
    private static final StringDecoder instance = new StringDecoder();

    public static StringDecoder instance() {
        return instance;
    }

    @Override
    public String decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        return readLengthEncodedString(buffer, settings.charset());
    }
}