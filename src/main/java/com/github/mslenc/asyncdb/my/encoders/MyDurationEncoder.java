package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

public class MyDurationEncoder extends MyValueEncoder<Duration> {
    public static final MyDurationEncoder instance = new MyDurationEncoder();

    @Override
    public int binaryFieldType(Duration value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_TIME;
    }

    private static final Duration MAX_DURATION = Duration.ofSeconds( 838 * 3600 + 59 * 60 + 59);
    private static final Duration MIN_DURATION = Duration.ofSeconds(-838 * 3600 - 59 * 60 - 59);

    public static int adjustedSeconds(Duration value) {
        int totalSeconds = (int) value.getSeconds();

        if (totalSeconds < 0) {
            if (value.getNano() != 0) {
                return -totalSeconds - 1;
            } else {
                return -totalSeconds;
            }
        } else {
            return totalSeconds;
        }
    }

    public static int adjustedMicros(Duration value) {
        if (value.isNegative()) {
            int nanos = value.getNano();
            if (nanos != 0) {
                return (1_000_000_000 - nanos) / 1000;
            } else {
                return 0;
            }
        } else {
            return value.getNano() / 1000;
        }
    }

    @Override
    public void encodeBinary(Duration value, ByteBuf out, MyEncoders encoders) {
        if (value.compareTo(MIN_DURATION) < 0 || value.compareTo(MAX_DURATION) > 0)
            throw new IllegalArgumentException("Duration is out of bounds: " + value);

        int totalSeconds = adjustedSeconds(value);
        int micros = adjustedMicros(value);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = (totalSeconds / 3600) % 24;
        int days = totalSeconds / 86400;

        boolean hasMicros = micros > 0;

        out.writeByte(hasMicros ? 12 : 8);
        out.writeByte(value.isNegative() ? 1 : 0);

        out.writeIntLE(days);
        out.writeByte(hours);
        out.writeByte(minutes);
        out.writeByte(seconds);

        if (hasMicros) {
            out.writeIntLE(micros);
        }
    }

    private static final int QUOTE_MINUS = 0x27_2D;        //  '-
    private static final int QUOTE = 0x27;                 //  '
    private static final int ZERO_ZERO = 0x30_30;          //  00
    private static final int ZERO_ZERO_ZERO = 0x30_30_30;  //  000
    private static final int COLON_ZERO_ZERO = 0x3A_30_30; //  :00

    @Override
    public void encodeAsSql(Duration value, ByteBuf out, MyEncoders encoders) {
        if (value.compareTo(MIN_DURATION) < 0 || value.compareTo(MAX_DURATION) > 0)
            throw new IllegalArgumentException("Duration is out of bounds: " + value);

        int totalSeconds = adjustedSeconds(value);
        int micros = adjustedMicros(value);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (value.isNegative()) {
            out.writeShort(QUOTE_MINUS); // '-
        } else {
            out.writeByte(QUOTE);
        }

        if (hours >= 100) {
            out.writeMedium(ZERO_ZERO_ZERO + ((hours / 100) << 16) + ((hours / 10 % 10) << 8) + (hours % 10));
        } else {
            out.writeShort(ZERO_ZERO + ((hours / 10 % 10) << 8) + (hours % 10));
        }

        out.writeMedium(COLON_ZERO_ZERO + ((minutes / 10) << 8) + (minutes % 10));
        out.writeMedium(COLON_ZERO_ZERO + ((seconds / 10) << 8) + (seconds % 10));

        EncUtils.writeMicrosAndQuote(micros, out);
    }
}
