package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.DbValue;
import io.netty.buffer.ByteBuf;

public class MyDbValueEncoder extends MyValueEncoder<DbValue> {
    public static final MyDbValueEncoder instance = new MyDbValueEncoder();

    @Override
    public boolean isNull(DbValue value, MyEncoders encoders) {
        return value == null || value.isNull();
    }

    @Override
    public int binaryFieldType(DbValue value, MyEncoders encoders) {
        MyValueEncoder<? super Object> actual = encoders.encoderFor(value.unwrap());
        return actual.binaryFieldType(value.unwrap(), encoders);
    }

    @Override
    public boolean isLongBinaryValue(DbValue value, MyEncoders encoders) {
        MyValueEncoder<? super Object> actual = encoders.encoderFor(value.unwrap());
        return actual.isLongBinaryValue(value.unwrap(), encoders);
    }

    @Override
    public ByteBuf encodeLongBinary(DbValue value, MyEncoders encoders) {
        MyValueEncoder<? super Object> actual = encoders.encoderFor(value.unwrap());
        return actual.encodeLongBinary(value.unwrap(), encoders);
    }

    @Override
    public void encodeBinary(DbValue value, ByteBuf out, MyEncoders encoders) {
        MyValueEncoder<? super Object> actual = encoders.encoderFor(value.unwrap());
        actual.encodeBinary(value.unwrap(), out, encoders);
    }

    @Override
    public void encodeAsSql(DbValue value, ByteBuf out, MyEncoders encoders) {
        MyValueEncoder<? super Object> actual = encoders.encoderFor(value.unwrap());
        actual.encodeAsSql(value.unwrap(), out, encoders);
    }
}
