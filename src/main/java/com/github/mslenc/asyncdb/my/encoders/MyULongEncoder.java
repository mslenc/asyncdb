package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ULong;
import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

public class MyULongEncoder extends MyValueEncoder<ULong> {
    public static final MyULongEncoder instance = new MyULongEncoder();

    @Override
    public int binaryFieldType(ULong value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_LONGLONG | MyConstants.PREP_STMT_UNSIGNED_FLAG;
    }

    @Override
    public void encodeBinary(ULong value, ByteBuf out, MyEncoders encoders) {
        out.writeLongLE(value.longValue());
    }

    @Override
    public void encodeAsSql(ULong value, ByteBuf out, MyEncoders encoders) {
        long longVal = value.longValue();

        if (longVal >= 0) {
            ByteBufUtils.appendAsciiInteger(longVal, out);
        } else {
            ByteBufUtils.appendAsciiString(value.toString(), out);
        }
    }
}
