package com.github.mslenc.asyncdb.my.encoders;

import io.netty.buffer.ByteBuf;

import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent") // we check for presence in isNull(), which is always called before any other method
public class MyOptionalEncoder extends MyValueEncoder<Optional> {
    public static final MyOptionalEncoder instance = new MyOptionalEncoder();

    @Override
    public boolean isNull(Optional value, MyEncoders encoders) {
        return value == null || !value.isPresent();
    }

    @Override
    public boolean isLongBinaryValue(Optional value, MyEncoders encoders) {
        Object theValue = value.get();
        MyValueEncoder<? super Object> actual = encoders.encoderFor(theValue);
        return actual.isLongBinaryValue(theValue, encoders);
    }

    @Override
    public int binaryFieldType(Optional value, MyEncoders encoders) {
        Object theValue = value.get();
        MyValueEncoder<? super Object> actual = encoders.encoderFor(theValue);
        return actual.binaryFieldType(theValue, encoders);
    }

    @Override
    public void encodeBinary(Optional value, ByteBuf out, MyEncoders encoders) {
        Object theValue = value.get();
        MyValueEncoder<? super Object> actual = encoders.encoderFor(theValue);
        actual.encodeBinary(theValue, out, encoders);
    }

    @Override
    public ByteBuf encodeLongBinary(Optional value, MyEncoders encoders) {
        Object theValue = value.get();
        MyValueEncoder<? super Object> actual = encoders.encoderFor(theValue);
        return actual.encodeLongBinary(theValue, encoders);
    }

    @Override
    public void encodeAsSql(Optional value, ByteBuf out, MyEncoders encoders) {
        Object theValue = value.get();
        MyValueEncoder<? super Object> actual = encoders.encoderFor(theValue);
        actual.encodeAsSql(theValue, out, encoders);
    }
}
