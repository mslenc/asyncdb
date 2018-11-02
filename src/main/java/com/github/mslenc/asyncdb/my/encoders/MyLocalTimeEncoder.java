package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.time.LocalTime;

public class MyLocalTimeEncoder extends MyValueEncoder<LocalTime> {
    public static final MyLocalTimeEncoder instance = new MyLocalTimeEncoder();

    @Override
    public int binaryFieldType(LocalTime value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_TIME;
    }

    @Override
    public void encodeBinary(LocalTime value, ByteBuf out, MyEncoders encoders) {
        int micros = value.getNano() / 1000;
        int seconds = value.getSecond();
        int minutes = value.getMinute();
        int hours = value.getHour();

        boolean hasMicros = micros > 0;

        out.writeByte(hasMicros ? 12 : 8);
        out.writeByte(0); // -> not negative

        out.writeInt(0); // -> days
        out.writeByte(hours);
        out.writeByte(minutes);
        out.writeByte(seconds);

        if (hasMicros) {
            out.writeIntLE(micros);
        }
    }

    private static final int COLON_ZERO_ZERO = 0x3A_30_30; //  :00
    private static final int QUOTE_ZERO_ZERO = 0x27_30_30; //  '00

    @Override
    public void encodeAsSql(LocalTime value, ByteBuf out, MyEncoders encoders) {
        int micro = value.getNano() / 1000;
        int second = value.getSecond();
        int minute = value.getMinute();
        int hour = value.getHour();

        out.writeMedium(QUOTE_ZERO_ZERO + ((hour   / 10) << 8) + (hour   % 10));
        out.writeMedium(COLON_ZERO_ZERO + ((minute / 10) << 8) + (minute % 10));
        out.writeMedium(COLON_ZERO_ZERO + ((second / 10) << 8) + (second % 10));

        EncUtils.writeMicrosAndQuote(micro, out);
    }
}
