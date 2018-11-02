package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MyBigIntegerEncoder extends MyValueEncoder<BigInteger> {
    public static final MyBigIntegerEncoder instance = new MyBigIntegerEncoder();

    @Override
    public int binaryFieldType(BigInteger value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_VARCHAR;
    }

    @Override
    public void encodeBinary(BigInteger value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.writeLengthEncodedString(value.toString(10), UTF_8, out);
    }

    @Override
    public void encodeAsSql(BigInteger value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.appendAsciiString(value.toString(10), out);
    }
}
