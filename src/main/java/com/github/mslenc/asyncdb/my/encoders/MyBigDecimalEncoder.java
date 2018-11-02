package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MyBigDecimalEncoder extends MyValueEncoder<BigDecimal> {
    public static final MyBigDecimalEncoder instance = new MyBigDecimalEncoder();

    @Override
    public int binaryFieldType(BigDecimal value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_VARCHAR;
    }

    @Override
    public void encodeBinary(BigDecimal value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.writeLengthEncodedString(value.toPlainString(), UTF_8, out);
    }

    @Override
    public void encodeAsSql(BigDecimal value, ByteBuf out, MyEncoders encoders) {
        // we don't want E (exponent), to keep the value exact (MySQL treats numbers with E as floating-points)
        ByteBufUtils.appendAsciiString(value.toPlainString(), out);
    }
}
