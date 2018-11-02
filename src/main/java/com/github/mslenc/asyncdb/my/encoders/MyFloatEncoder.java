package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyFloatEncoder extends MyValueEncoder<Float> {
    public static final MyFloatEncoder instance = new MyFloatEncoder();

    @Override
    public int binaryFieldType(Float value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_FLOAT;
    }

    @Override
    public void encodeBinary(Float value, ByteBuf out, MyEncoders encoders) {
        out.writeFloatLE(value);
    }

    @Override
    public void encodeAsSql(Float value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.appendAsciiString(value.toString(), out);
    }
}
