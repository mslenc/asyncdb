package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyIntegerEncoder extends MyValueEncoder<Integer> {
    public static final MyIntegerEncoder instance = new MyIntegerEncoder();

    @Override
    public int binaryFieldType(Integer value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_LONG;
    }

    @Override
    public void encodeBinary(Integer value, ByteBuf out, MyEncoders encoders) {
        out.writeIntLE(value);
    }

    @Override
    public void encodeAsSql(Integer value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.appendAsciiInteger(value, out);
    }
}
