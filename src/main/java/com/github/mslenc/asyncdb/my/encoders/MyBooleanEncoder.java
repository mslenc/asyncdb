package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyBooleanEncoder extends MyValueEncoder<Boolean> {
    public static final MyBooleanEncoder instance = new MyBooleanEncoder();

    @Override
    public int binaryFieldType(Boolean value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_TINY;
    }

    @Override
    public void encodeBinary(Boolean value, ByteBuf out, MyEncoders encoders) {
        out.writeByte(value ? 1 : 0);
    }

    @Override
    public void encodeAsSql(Boolean value, ByteBuf out, MyEncoders encoders) {
        out.writeByte(value ? '1' : '0');
    }
}
