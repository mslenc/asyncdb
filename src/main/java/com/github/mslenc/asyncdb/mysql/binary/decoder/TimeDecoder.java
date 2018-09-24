package com.github.mslenc.asyncdb.mysql.binary.decoder;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.ex.DecodingException;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

public class TimeDecoder implements BinaryDecoder {
    private static final TimeDecoder instance = new TimeDecoder();

    public static TimeDecoder instance() {
        return instance;
    }

    @Override
    public Duration decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        int len = buffer.readUnsignedByte();

        switch (len) {
            case 0:
                return Duration.ZERO;

            case 8: {
                boolean negative = buffer.readUnsignedByte() == 1;

                Duration result = Duration.ZERO;
                result = result.plus(Duration.ofDays(buffer.readUnsignedIntLE()));
                result = result.plus(Duration.ofHours(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofMinutes(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofSeconds(buffer.readUnsignedByte()));

                return negative ? result.negated() : result;
            }

            case 12: {
                boolean negative = buffer.readUnsignedByte() == 1;

                Duration result = Duration.ZERO;
                result = result.plus(Duration.ofDays(buffer.readUnsignedIntLE()));
                result = result.plus(Duration.ofHours(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofMinutes(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofSeconds(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofNanos(1000L * buffer.readUnsignedIntLE())); // mysql has micros, we have nanos

                return negative ? result.negated() : result;
            }

            default:
                throw new DecodingException("Unexpected length for a duration (" + len + ")");
        }
    }
}
