package com.github.mslenc.asyncdb.mysql.column;

import com.github.mslenc.asyncdb.common.column.ColumnDecoder;

import java.time.Duration;

public class DurationDecoder implements ColumnDecoder {
    private static final DurationDecoder instance = new DurationDecoder();

    public static DurationDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(String value) {
        String[] parts = value.split("[.:]");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        Duration hms = Duration.ofSeconds(3600L * hours + 60L * minutes + seconds);

        if (parts.length > 3) {
            String last = parts[3];
            int multiplier;

            switch (last.length()) {
                case 1: multiplier = 100000000; break;
                case 2: multiplier = 10000000; break;
                case 3: multiplier = 1000000; break;
                case 4: multiplier = 100000; break;
                case 5: multiplier = 10000; break;
                case 6: multiplier = 1000; break;
                case 7: multiplier = 100; break;
                case 8: multiplier = 10; break;
                case 9: multiplier = 1; break;

                default: // ???
                    return hms;
            }

            long nanos = Long.parseLong(last) * multiplier;

            return hms.plusNanos(nanos);
        } else {
            return hms;
        }
    }
}