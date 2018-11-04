package com.github.mslenc.asyncdb.my.encoders;

import io.netty.buffer.ByteBuf;

public class EncUtils {
    static void writeMicrosAndQuote(int micros, ByteBuf out) {
        if (micros > 0) {
            out.writeByte('.');

            int div = 100000;
            while (micros > 0) {
                out.writeByte('0' + (micros / div));
                micros %= div;
                div /= 10;
            }
        }

        out.writeByte('\'');
    }

    public static void writeMicros(int micros, StringBuilder out) {
        if (micros > 0) {
            out.append('.');

            int div = 100000;
            while (micros > 0) {
                out.append(micros / div);
                micros %= div;
                div /= 10;
            }
        }
    }
}
