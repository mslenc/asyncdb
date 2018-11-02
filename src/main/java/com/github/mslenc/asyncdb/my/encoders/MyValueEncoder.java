package com.github.mslenc.asyncdb.my.encoders;

import io.netty.buffer.ByteBuf;

public abstract class MyValueEncoder<T> {
    /**
     * Returns whether the value is considered null. Normally, that is the case only when the value
     * is in fact null, but sometimes a special object is used instead.
     */
    public boolean isNull(T value, MyEncoders encoders) {
        return value == null;
    }

    /**
     * Returns true if the value is large (in terms of byte length) and should be sent separately in a
     * STMT_SEND_LONG_DATA packet.
     */
    public boolean isLongBinaryValue(T value, MyEncoders encoders) {
        return false;
    }

    /**
     * Returns the MySQL field type (see MySQLIO.FIELD_TYPE_*) for this value.
     */
    public abstract int binaryFieldType(T value, MyEncoders encoders);

    /**
     * Encodes the value in binary form into the specified buffer.
     */
    public abstract void encodeBinary(T value, ByteBuf out, MyEncoders encoders);

    /**
     * Packs the value into a ByteBuf appropriate for sending as a STMT_SEND_LONG_DATA packet.
     */
    public ByteBuf encodeLongBinary(T value, MyEncoders encoders) {
        throw new UnsupportedOperationException();
    }

    /**
     * Encodes the value into a textual (SQL) form, applying any necessary escaping and whatnot.
     * The encoding of the ByteBuf is always UTF-8.
     */
    public abstract void encodeAsSql(T value, ByteBuf out, MyEncoders encoders);
}
