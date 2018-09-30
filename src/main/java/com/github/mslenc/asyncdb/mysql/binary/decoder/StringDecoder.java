package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class StringDecoder implements BinaryDecoder {
    private static final StringDecoder instance = new StringDecoder();

    public static StringDecoder instance() {
        return instance;
    }

    @Override
    public String decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        return ByteBufUtils.readLengthEncodedString(buffer, StandardCharsets.UTF_8);
    }
}