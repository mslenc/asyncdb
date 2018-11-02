package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyDoubleEncoder extends MyValueEncoder<Double> {
    public static final MyDoubleEncoder instance = new MyDoubleEncoder();

    @Override
    public int binaryFieldType(Double value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_DOUBLE;
    }

    @Override
    public void encodeBinary(Double value, ByteBuf out, MyEncoders encoders) {
        out.writeDoubleLE(value);
    }

    @Override
    public void encodeAsSql(Double value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.appendAsciiString(value.toString(), out);
    }
}
