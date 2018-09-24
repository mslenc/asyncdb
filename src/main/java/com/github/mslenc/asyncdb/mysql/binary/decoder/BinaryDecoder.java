package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public interface BinaryDecoder {
    Object decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData);
}
