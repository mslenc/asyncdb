package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyLongEncoder extends MyValueEncoder<Long> {
    public static final MyLongEncoder instance = new MyLongEncoder();

    @Override
    public int binaryFieldType(Long value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_LONGLONG;
    }

    @Override
    public void encodeBinary(Long value, ByteBuf out, MyEncoders encoders) {
        out.writeLongLE(value);
    }

    @Override
    public void encodeAsSql(Long value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.appendAsciiInteger(value, out);
    }
}
