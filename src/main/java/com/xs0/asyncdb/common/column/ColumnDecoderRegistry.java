package com.xs0.asyncdb.common.column;

import java.nio.charset.Charset;

import com.xs0.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

interface ColumnDecoderRegistry {
    Object decode(ColumnData kind, ByteBuf value, Charset charset);
}
