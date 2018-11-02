package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class MyLocalDateTimeEncoder extends MyValueEncoder<LocalDateTime> {
    public static final MyLocalDateTimeEncoder instance = new MyLocalDateTimeEncoder();

    private static final LocalDateTime MIN_DATE_TIME = LocalDateTime.of(1000,  1,  1,  0,  0,  0,         0);
    private static final LocalDateTime MAX_DATE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999999999);

    @Override
    public int binaryFieldType(LocalDateTime value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_TIMESTAMP;
    }

    @Override
    public void encodeBinary(LocalDateTime value, ByteBuf out, MyEncoders encoders) {
        if (value.compareTo(MIN_DATE_TIME) < 0 || value.compareTo(MAX_DATE_TIME) > 0)
            throw new IllegalArgumentException("LocalDateTime is out of bounds: " + value);

        int micros = value.getNano() / 1000;
        boolean hasMicros = micros != 0;

        out.writeByte(hasMicros ? 11 : 7);

        out.writeShortLE(value.getYear());
        out.writeByte(value.getMonthValue());
        out.writeByte(value.getDayOfMonth());
        out.writeByte(value.getHour());
        out.writeByte(value.getMinute());
        out.writeByte(value.getSecond());

        if (hasMicros) {
            out.writeIntLE(micros);
        }
    }

    private static final int QUOTE = 0x27;                        // '
    private static final int ZERO_ZERO_ZERO_ZERO = 0x30_30_30_30; // 0000
    private static final int MINUS_ZERO_ZERO = 0x2D_30_30;        // -00
    private static final int SPACE_ZERO_ZERO = 0x20_30_30;        //  00
    private static final int COLON_ZERO_ZERO = 0x3A_30_30;        // :00


    @Override
    public void encodeAsSql(LocalDateTime value, ByteBuf out, MyEncoders encoders) {
        if (value.compareTo(MIN_DATE_TIME) < 0 || value.compareTo(MAX_DATE_TIME) > 0)
            throw new IllegalArgumentException("LocalDateTime is out of bounds: " + value);

        int year = value.getYear();
        int month = value.getMonthValue();
        int day = value.getDayOfMonth();
        int hour = value.getHour();
        int minute = value.getMinute();
        int second = value.getSecond();
        int micro = value.getNano() / 1000;

        int startPos = out.writerIndex();

        out.writeByte(QUOTE);
        out.writeInt(ZERO_ZERO_ZERO_ZERO + ((year / 1000) << 24) + ((year / 100 % 10) << 16) + ((year / 10 % 10) << 8) + (year % 10));

        out.writeMedium(MINUS_ZERO_ZERO + ((month  / 10) << 8) + (month  % 10));
        out.writeMedium(MINUS_ZERO_ZERO + ((day    / 10) << 8) + (day    % 10));
        out.writeMedium(SPACE_ZERO_ZERO + ((hour   / 10) << 8) + (hour   % 10));
        out.writeMedium(COLON_ZERO_ZERO + ((minute / 10) << 8) + (minute % 10));
        out.writeMedium(COLON_ZERO_ZERO + ((second / 10) << 8) + (second % 10));

        EncUtils.writeMicrosAndQuote(micro, out);

        int endPos = out.writerIndex();

        log.debug("Converted {} into -->{}<--", value, out.toString(startPos, endPos - startPos, StandardCharsets.UTF_8));
    }

    private static final Logger log = LoggerFactory.getLogger(MyLocalDateTimeEncoder.class);
}
