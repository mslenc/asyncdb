package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.time.Year;

public class MyYearEncoder extends MyValueEncoder<Year> {
    public static final MyYearEncoder instance = new MyYearEncoder();

    private static final int MIN_YEAR = 1901;
    private static final int MAX_YEAR = 2155;

    @Override
    public int binaryFieldType(Year value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_SHORT;
    }

    @Override
    public void encodeBinary(Year value, ByteBuf out, MyEncoders encoders) {
        int year = value.getValue();
        if (year < MIN_YEAR || year > MAX_YEAR)
            throw new IllegalArgumentException("Year is out of bounds: " + value);

        out.writeShortLE(year);
    }

    private static final int ZERO_ZERO_ZERO_ZERO = 0x30_30_30_30;

    @Override
    public void encodeAsSql(Year value, ByteBuf out, MyEncoders encoders) {
        int year = value.getValue();
        if (year < MIN_YEAR || year > MAX_YEAR)
            throw new IllegalArgumentException("Year is out of bounds: " + value);

        out.writeInt(ZERO_ZERO_ZERO_ZERO + ((year / 1000) << 24) + ((year / 100 % 10) << 16) + ((year / 10 % 10) << 8) + (year % 10));
    }
}
