package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import io.netty.buffer.ByteBuf;

public abstract class ByteDecoder implements BinaryDecoder {
    private static final ByteDecoder instance_signed = new ByteDecoder() {
        public Byte decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return buffer.readByte();
        }
    };

    private static final ByteDecoder instance_unsigned = new ByteDecoder() {
        public Short decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
            return buffer.readUnsignedByte();
        }
    };

    public static ByteDecoder instance(boolean unsigned) {
        return unsigned ? instance_unsigned : instance_signed;
    }
}
