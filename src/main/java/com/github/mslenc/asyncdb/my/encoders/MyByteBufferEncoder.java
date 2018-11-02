package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

import static com.github.mslenc.asyncdb.my.encoders.MyByteArrayEncoder.*;

public class MyByteBufferEncoder extends MyValueEncoder<ByteBuffer> {
    public static final MyByteBufferEncoder instance = new MyByteBufferEncoder();

    @Override
    public int binaryFieldType(ByteBuffer value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_LONG_BLOB;
    }

    @Override
    public void encodeBinary(ByteBuffer value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.writeLength(value.remaining(), out);
        out.writeBytes(value);
    }

    @Override
    public boolean isLongBinaryValue(ByteBuffer value, MyEncoders encoders) {
        return value.remaining() >= MIN_LONG_VAL_LENGTH;
    }

    @Override
    public ByteBuf encodeLongBinary(ByteBuffer value, MyEncoders encoders) {
        return Unpooled.wrappedBuffer(value);
    }

    @Override
    public void encodeAsSql(ByteBuffer value, ByteBuf out, MyEncoders encoders) {
        final short[] hexShorts = MyByteArrayEncoder.hexShorts;

        out.writeShort(OPEN_X_QUOTE);

        int pos = value.position();
        int lastPos = pos + value.remaining();

        while (pos < lastPos) {
            out.writeShort(hexShorts[value.get(pos++) & 255]);
        }

        value.position(lastPos);

        out.writeByte(CLOSE_QUOTE);
    }
}
