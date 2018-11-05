package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MyStringEncoder extends MyValueEncoder<String> {
    public static final MyStringEncoder instance = new MyStringEncoder();

    @Override
    public void encodeBinary(String value, ByteBuf out, MyEncoders encoders) {
        ByteBufUtils.writeLengthEncodedString(value, UTF_8, out);
    }

    @Override
    public int binaryFieldType(String value, MyEncoders encoders) {
        return MyConstants.FIELD_TYPE_STRING;
    }

    @Override
    public boolean isLongBinaryValue(String value, MyEncoders encoders) {
        return value.length() >= 1024;
    }

    @Override
    public ByteBuf encodeLongBinary(String value, MyEncoders encoders) {
        return Unpooled.copiedBuffer(value, UTF_8);
    }

    @Override
    public void encodeAsSql(String value, ByteBuf out, MyEncoders encoders) {
        out.writeByte('\'');

        int pos = 0, len = value.length();
        while (pos < len) {
            int codepoint = value.codePointAt(pos);
            pos += codepoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT ? 2 : 1;

            escapeCharacter(codepoint, out);
        }

        out.writeByte('\'');
    }

    private static final int BACKSLASH = '\\' << 8;
    static void escapeCharacter(int codepoint, ByteBuf out) {
        switch (codepoint) {
            case 0:
                out.writeShort(BACKSLASH | '0');
                break;

            case 26:
                out.writeShort(BACKSLASH | 'Z');
                break;

            case '\b':
                out.writeShort(BACKSLASH | 'b');
                break;

            case '\t':
                out.writeShort(BACKSLASH | 't');
                break;

            case '\n':
                out.writeShort(BACKSLASH | 'n');
                break;

            case '\r':
                out.writeShort(BACKSLASH | 'r');
                break;

            case '"':
                out.writeShort(BACKSLASH | '"');
                break;

            case '\\':
                out.writeShort(BACKSLASH | '\\');
                break;

            case '\'':
                out.writeShort(BACKSLASH | '\'');
                break;

            default:
                ByteBufUtils.appendUtf8Codepoint(codepoint, out);
                break;
        }
    }
}
