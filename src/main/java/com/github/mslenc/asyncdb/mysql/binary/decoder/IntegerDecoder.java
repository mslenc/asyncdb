package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

public abstract class IntegerDecoder implements BinaryDecoder {
    private static final IntegerDecoder instance_signed = new IntegerDecoder() {
        public Integer decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return buffer.readIntLE();
        }
    };

    private static final IntegerDecoder instance_unsigned = new IntegerDecoder() {
        public Long decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return buffer.readUnsignedIntLE();
        }
    };

    public static IntegerDecoder instance(boolean unsigned) {
        return unsigned ? instance_unsigned : instance_signed;
    }
}
