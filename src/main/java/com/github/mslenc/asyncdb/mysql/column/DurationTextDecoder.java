package com.github.mslenc.asyncdb.mysql.column;

import java.time.Duration;

public class DurationTextDecoder implements TextValueDecoder {
    private static final DurationTextDecoder instance = new DurationTextDecoder();

    public static DurationTextDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(String value) {
        String[] parts = value.split("[.:]");

        boolean negative = value.startsWith("-");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        if (negative) {
            minutes *= -1;
            seconds *= -1;
        }

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

            if (negative) {
                return hms.minusNanos(nanos);
            } else {
                return hms.plusNanos(nanos);
            }
        } else {
            return hms;
        }
    }
}