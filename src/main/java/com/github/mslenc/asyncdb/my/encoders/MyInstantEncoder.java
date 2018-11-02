package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class MyInstantEncoder extends MyValueEncoder<Instant> {
    public static final MyInstantEncoder instance = new MyInstantEncoder();

    @Override
    public int binaryFieldType(Instant value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_TIMESTAMP;
    }

    @Override
    public void encodeBinary(Instant value, ByteBuf out, MyEncoders encoders) {
        LocalDateTime ldt = LocalDateTime.ofInstant(value, ZoneOffset.UTC);
        MyLocalDateTimeEncoder.instance.encodeBinary(ldt, out, encoders);
    }

    @Override
    public void encodeAsSql(Instant value, ByteBuf out, MyEncoders encoders) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(value, ZoneOffset.UTC);
        MyLocalDateTimeEncoder.instance.encodeAsSql(dateTime.toLocalDateTime(), out, encoders);
    }
}
