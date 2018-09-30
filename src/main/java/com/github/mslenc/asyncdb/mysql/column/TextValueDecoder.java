package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public interface TextValueDecoder {
    Object decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings);
}
