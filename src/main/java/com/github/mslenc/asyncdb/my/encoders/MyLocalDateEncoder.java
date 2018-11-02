package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.time.LocalDate;

public class MyLocalDateEncoder extends MyValueEncoder<LocalDate> {
    public static final MyLocalDateEncoder instance = new MyLocalDateEncoder();

    private static final LocalDate MIN_DATE = LocalDate.of(1000,  1,  1);
    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    @Override
    public int binaryFieldType(LocalDate value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_DATE;
    }

    @Override
    public void encodeBinary(LocalDate value, ByteBuf out, MyEncoders encoders) {
        if (value.compareTo(MIN_DATE) < 0 || value.compareTo(MAX_DATE) > 0)
            throw new IllegalArgumentException("LocalDate is out of bounds: " + value);

        out.writeByte(4);
        out.writeShortLE(value.getYear());
        out.writeByte(value.getMonthValue());
        out.writeByte(value.getDayOfMonth());
    }

    private static final int QUOTE_ZERO_ZERO = 0x27_30_30;        // '00
    private static final int ZERO_ZERO_MINUS = 0x30_30_2D;        // 00-
    private static final int ZERO_ZERO_QUOTE = 0x30_30_27;        // 00'

    @Override
    public void encodeAsSql(LocalDate value, ByteBuf out, MyEncoders encoders) {
        if (value.compareTo(MIN_DATE) < 0 || value.compareTo(MAX_DATE) > 0)
            throw new IllegalArgumentException("LocalDate is out of bounds: " + value);

        int year = value.getYear();
        int month = value.getMonthValue();
        int day = value.getDayOfMonth();

        out.writeMedium(QUOTE_ZERO_ZERO + ((year  / 1000     ) <<  8) +  (year  / 100 % 10)      )
           .writeMedium(ZERO_ZERO_MINUS + ((year  /   10 % 10) << 16) + ((year  % 10      ) << 8))
           .writeMedium(ZERO_ZERO_MINUS + ((month /   10     ) << 16) + ((month % 10      ) << 8))
           .writeMedium(ZERO_ZERO_QUOTE + ((day   /   10     ) << 16) + ((day   % 10      ) << 8));
    }
}
