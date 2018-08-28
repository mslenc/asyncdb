package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.mysql.ex.DecodingException;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

public class TimeDecoder implements BinaryDecoder {
    private static final TimeDecoder instance = new TimeDecoder();

    public static TimeDecoder instance() {
        return instance;
    }

    @Override
    public Duration decode(ByteBuf buffer) {
        int len = buffer.readUnsignedByte();

        switch (len) {
            case 0:
                return Duration.ZERO;

            case 8: {
                boolean negative = buffer.readUnsignedByte() == 1;

                Duration result = Duration.ZERO;
                result = result.plus(Duration.ofDays(buffer.readUnsignedInt()));
                result = result.plus(Duration.ofHours(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofMinutes(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofSeconds(buffer.readUnsignedByte()));

                return negative ? result.negated() : result;
            }

            case 12: {
                boolean negative = buffer.readUnsignedByte() == 1;

                Duration result = Duration.ZERO;
                result = result.plus(Duration.ofDays(buffer.readUnsignedInt()));
                result = result.plus(Duration.ofHours(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofMinutes(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofSeconds(buffer.readUnsignedByte()));
                result = result.plus(Duration.ofNanos(1000L * buffer.readUnsignedInt())); // mysql has micros, we have nanos

                return negative ? result.negated() : result;
            }

            default:
                throw new DecodingException("Unexpected length for a duration (" + len + ")");
        }
    }
}
