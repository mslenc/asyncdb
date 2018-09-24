package com.github.mslenc.asyncdb.common.column;

import java.nio.charset.Charset;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

interface ColumnDecoderRegistry {
    Object decode(ColumnData kind, ByteBuf value, Charset charset);
}
