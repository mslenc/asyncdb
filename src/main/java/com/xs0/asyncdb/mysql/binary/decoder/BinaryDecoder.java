package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

public interface BinaryDecoder {
    Object decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData);
}
