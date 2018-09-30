package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.time.Duration;

public class DurationTextDecoder implements TextValueDecoder {
    private static final DurationTextDecoder instance = new DurationTextDecoder();

    public static DurationTextDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(ColumnData kind, ByteBuf packet, int byteLength, CodecSettings codecSettings) {
        int part = 0;
        boolean negative = false;
        int hours = 0, minutes = 0, seconds = 0, nanos = 0;
        int nanoLength = 0;
        int remain = byteLength;

        while (remain-- > 0) {
            int c = packet.readUnsignedByte();

            switch (part) {
                case 0: // hours
                    if (c == '-') {
                        negative = true;
                    } else
                    if (c == ':') {
                        part = 1;
                    } else {
                        hours = 10 * hours + (c - '0');
                    }
                    break;

                case 1: // minutes
                    if (c == ':') {
                        part = 2;
                    } else {
                        minutes = 10 * minutes + (c - '0');
                    }
                    break;

                case 2: // seconds
                    if (c == '.') {
                        part = 3;
                    } else {
                        seconds = 10 * seconds + (c - '0');
                    }
                    break;

                case 3: // nanos
                    nanos = 10 * nanos + (c - '0');
                    nanoLength++;
                    break;
            }
        }

        if (nanoLength > 0) {
            while (nanoLength < 9) {
                nanos *= 10;
                nanoLength++;
            }
        }

        long totalSeconds = 3600L * hours + 60L * minutes + seconds;

        if (negative) {
            return Duration.ofSeconds(-totalSeconds, -nanos);
        } else {
            return Duration.ofSeconds(totalSeconds, nanos);
        }
    }
}