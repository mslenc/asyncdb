package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyByteEncoder extends MyValueEncoder<Byte> {
    public static final MyByteEncoder instance = new MyByteEncoder();

    @Override
    public int binaryFieldType(Byte value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_TINY;
    }

    @Override
    public void encodeBinary(Byte value, ByteBuf out, MyEncoders encoders) {
        out.writeByte(value);
    }

    @Override
    public void encodeAsSql(Byte value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.appendAsciiInteger(value, out);
    }
}
