package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import static com.github.mslenc.asyncdb.my.encoders.MyByteArrayEncoder.CLOSE_QUOTE;
import static com.github.mslenc.asyncdb.my.encoders.MyByteArrayEncoder.MIN_LONG_VAL_LENGTH;
import static com.github.mslenc.asyncdb.my.encoders.MyByteArrayEncoder.OPEN_X_QUOTE;

public class MyByteBufEncoder extends MyValueEncoder<ByteBuf> {
    public static final MyByteBufEncoder instance = new MyByteBufEncoder();

    @Override
    public int binaryFieldType(ByteBuf value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_LONG_BLOB;
    }

    @Override
    public void encodeBinary(ByteBuf value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.writeLength(value.readableBytes(), out);
        out.writeBytes(value);
    }

    @Override
    public boolean isLongBinaryValue(ByteBuf value, MyEncoders encoders) {
        return value.readableBytes() >= MIN_LONG_VAL_LENGTH;
    }

    @Override
    public ByteBuf encodeLongBinary(ByteBuf value, MyEncoders encoders) {
        return value;
    }

    @Override
    public void encodeAsSql(ByteBuf value, ByteBuf out, MyEncoders encoders) {
        final short[] hexShorts = MyByteArrayEncoder.hexShorts;

        out.writeShort(OPEN_X_QUOTE);

        int pos = value.readerIndex();
        int lastPos = pos + value.readableBytes();

        while (pos < lastPos) {
            out.writeShort(hexShorts[value.getByte(pos++) & 255]);
        }

        value.readerIndex(lastPos);

        out.writeByte(CLOSE_QUOTE);
    }
}
