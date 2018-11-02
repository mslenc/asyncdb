package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyShortEncoder extends MyValueEncoder<Short> {
    public static final MyShortEncoder instance = new MyShortEncoder();

    @Override
    public int binaryFieldType(Short value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_SHORT;
    }

    @Override
    public void encodeBinary(Short value, ByteBuf out, MyEncoders encoders) {
        out.writeShortLE(value);
    }

    @Override
    public void encodeAsSql(Short value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.appendAsciiInteger(value, out);
    }
}
