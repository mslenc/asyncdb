package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MyByteArrayEncoder extends MyValueEncoder<byte[]> {
    public static final MyByteArrayEncoder instance = new MyByteArrayEncoder();

    static final int MIN_LONG_VAL_LENGTH = 1024;

    @Override
    public int binaryFieldType(byte[] value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_LONG_BLOB;
    }

    @Override
    public void encodeBinary(byte[] value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.writeLength(value.length, out);
        out.writeBytes(value);
    }

    @Override
    public boolean isLongBinaryValue(byte[] value, MyEncoders encoders) {
        return value.length >= MIN_LONG_VAL_LENGTH;
    }

    @Override
    public ByteBuf encodeLongBinary(byte[] value, MyEncoders encoders) {
        return Unpooled.wrappedBuffer(value);
    }

    static final short OPEN_X_QUOTE = 0x78_27; // x'
    static final byte CLOSE_QUOTE   = 0x27;    // '

    static final short[] hexShorts = new short[256];
    static {
        byte[] hexBytes = "0123456789ABCDEF".getBytes(UTF_8);
        for (int a = 0; a < 256; a++) {
            hexShorts[a] = (short) (hexBytes[a >> 4] << 8 | hexBytes[a & 15]);
        }
    }

    @Override
    public void encodeAsSql(byte[] value, ByteBuf out, MyEncoders encoders) {
        final short[] hexShorts = MyByteArrayEncoder.hexShorts;

        out.writeShort(OPEN_X_QUOTE);

        for (byte b : value)
            out.writeShort(hexShorts[b & 255]);

        out.writeByte(CLOSE_QUOTE);
    }
}
