package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.common.ULong;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

public abstract class LongDecoder implements BinaryDecoder {
    private static final LongDecoder instance_signed = new LongDecoder() {
        public Long decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return buffer.readLongLE();
        }
    };

    private static final LongDecoder instance_unsigned = new LongDecoder() {
        public ULong decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return ULong.valueOf(buffer.readLongLE());
        }
    };

    public static LongDecoder instance(boolean unsigned) {
        return unsigned ? instance_unsigned : instance_signed;
    }
}
