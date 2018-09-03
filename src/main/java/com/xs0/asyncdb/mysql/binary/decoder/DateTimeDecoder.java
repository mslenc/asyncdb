package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import com.xs0.asyncdb.mysql.ex.DecodingException;
import io.netty.buffer.ByteBuf;

import java.time.LocalDateTime;

public class DateTimeDecoder implements BinaryDecoder {
    private static final DateTimeDecoder instance = new DateTimeDecoder();

    public static DateTimeDecoder instance() {
        return instance;
    }

    @Override
    public LocalDateTime decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        int size = buffer.readUnsignedByte();

        switch (size) {
            case 0:
                return null;

            case 4:
                return LocalDateTime.of(
                    buffer.readUnsignedShortLE(), buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                    0, 0, 0, 0
                );

            case 7:
                return LocalDateTime.of(
                    buffer.readUnsignedShortLE(), buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                    buffer.readUnsignedByte(), buffer.readUnsignedByte(),buffer.readUnsignedByte(), 0
                );

            case 11:
                return LocalDateTime.of(
                        buffer.readUnsignedShortLE(), buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                        buffer.readUnsignedByte(), buffer.readUnsignedByte(),buffer.readUnsignedByte(),
                        (int)(buffer.readUnsignedIntLE() * 1000L)); // mysql is micros, LocalDateTime is nanos

            default:
                throw new DecodingException("Unexpected length for a timestamp (" + size + ")");
        }
    }
}
