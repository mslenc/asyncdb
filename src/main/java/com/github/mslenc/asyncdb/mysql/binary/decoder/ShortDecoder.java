package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.DecoderRegistry;
import io.netty.buffer.ByteBuf;

public abstract class ShortDecoder implements BinaryDecoder {
    private static final ShortDecoder instance_signed = new ShortDecoder() {
        public Short decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return buffer.readShortLE();
        }
    };

    private static final ShortDecoder instance_unsigned = new ShortDecoder() {
        public Integer decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return buffer.readUnsignedShortLE();
        }
    };

    public static ShortDecoder instance(boolean unsigned) {
        return unsigned ? instance_unsigned : instance_signed;
    }
}
