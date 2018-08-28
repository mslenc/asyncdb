package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.mysql.ex.DecodingException;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;

public class TimestampDecoder implements BinaryDecoder {
    private static final TimestampDecoder instance = new TimestampDecoder();

    public static TimestampDecoder instance() {
        return instance;
    }

    @Override
    public LocalDateTime decode(ByteBuf buffer) {
        int size = buffer.readUnsignedByte();

        switch (size) {
            case 0:
                return null;

            case 4:
                return LocalDateTime.of(
                    buffer.readUnsignedShort(), buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                    0, 0, 0, 0
                );

            case 7:
                return LocalDateTime.of(
                    buffer.readUnsignedShort(), buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                    buffer.readUnsignedByte(), buffer.readUnsignedByte(),buffer.readUnsignedByte(), 0
                );

            case 11:
                return LocalDateTime.of(
                        buffer.readUnsignedShort(), buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                        buffer.readUnsignedByte(), buffer.readUnsignedByte(),buffer.readUnsignedByte(),
                        (int)(buffer.readUnsignedInt() * 1000L)); // mysql is micros, LocalDateTime is nanos

            default:
                throw new DecodingException("Unexpected length for a timestamp (" + size + ")");
        }
    }
}
