package com.github.mslenc.asyncdb.my.resultset;

import com.github.mslenc.asyncdb.ex.ProtocolException;
import com.github.mslenc.asyncdb.my.MyDbColumn;
import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.ex.DecodingException;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.nio.charset.StandardCharsets;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.*;
import static com.github.mslenc.asyncdb.my.MyConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class MyDecodingResultSetBuilder<QR> implements MyResultSetBuilder<QR> {
    protected abstract void onBlobValue(MyDbColumn column, ByteBuf buffer, int length);
    protected abstract void onTextValue(MyDbColumn column, String string);

    protected abstract void onTimeValue(MyDbColumn column, boolean negative, int hours, int minutes, int seconds, int micros);
    protected abstract void onDateValue(MyDbColumn column, int year, int month, int day);
    protected abstract void onDateTimeValue(MyDbColumn column, int year, int month, int day, int hour, int minute, int second, int micro);
    protected abstract void onTimestampValue(MyDbColumn column, int year, int month, int day, int hour, int minute, int second, int micro);
    protected abstract void onYearValue(MyDbColumn column, int value);

    protected abstract void onFloatValue(MyDbColumn column, float value);
    protected abstract void onDoubleValue(MyDbColumn column, double value);

    protected abstract void onDecimalValue(MyDbColumn column, String valueStr);

    protected abstract void onTinyValue(MyDbColumn column, byte value);
    protected abstract void onUnsignedTinyValue(MyDbColumn column, short value);
    protected abstract void onShortValue(MyDbColumn column, short value);
    protected abstract void onUnsignedShortValue(MyDbColumn column, int value);
    protected abstract void onInt24Value(MyDbColumn column, int value);
    protected abstract void onUnsignedInt24Value(MyDbColumn column, int value);
    protected abstract void onLongValue(MyDbColumn column, int value);
    protected abstract void onUnsignedLongValue(MyDbColumn column, long value);
    protected abstract void onLongLongValue(MyDbColumn column, long value);
    protected abstract void onUnsignedLongLongValue(MyDbColumn column, long value);

    protected abstract void onGeometryValue(MyDbColumn column, ByteBuf buffer, int length);
    protected abstract void onJsonValue(MyDbColumn column, ByteBuf buffer, int length);

    @Override
    public void binaryValue(MyDbColumn column, ByteBuf buffer) {
        boolean unsigned = column.isUnsigned();

        switch (column.getDataType()) {
            case FIELD_TYPE_VARCHAR:
            case FIELD_TYPE_ENUM:
            case FIELD_TYPE_SET: {
                String string = readLengthEncodedString(buffer, StandardCharsets.UTF_8);
                onTextValue(column, string);
                return;
            }

            case FIELD_TYPE_STRING:
            case FIELD_TYPE_BLOB:
            case FIELD_TYPE_LONG_BLOB:
            case FIELD_TYPE_MEDIUM_BLOB:
            case FIELD_TYPE_TINY_BLOB:
            case FIELD_TYPE_VAR_STRING: {
                if (column.getCharsetId() == CHARSET_ID_BINARY) {
                    int length = (int) readBinaryLength(buffer);
                    onBlobValue(column, buffer, length);
                } else {
                    String string = readLengthEncodedString(buffer, StandardCharsets.UTF_8);
                    onTextValue(column, string);
                }
                return;
            }


            case FIELD_TYPE_BIT: {
                int length = (int) readBinaryLength(buffer);
                onBlobValue(column, buffer, length);
                return;
            }

            case FIELD_TYPE_LONGLONG: {
                long value = buffer.readLongLE();
                if (unsigned) {
                    onUnsignedLongLongValue(column, value);
                } else {
                    onLongLongValue(column, value);
                }
                return;
            }

            case FIELD_TYPE_LONG: {
                if (unsigned) {
                    long value = buffer.readUnsignedIntLE();
                    onUnsignedLongValue(column, value);
                } else {
                    int value = buffer.readIntLE();
                    onLongValue(column, value);
                }
                return;
            }

            case FIELD_TYPE_INT24: {
                if (unsigned) {
                    int value = buffer.readIntLE() & 0xFFFFFF; // yes, it's 4 bytes, not 3 bytes
                    onUnsignedInt24Value(column, value);
                } else {
                    int value = buffer.readIntLE();
                    onInt24Value(column, value);
                }
                return;
            }

            case FIELD_TYPE_SHORT: {
                if (unsigned) {
                    int value = buffer.readUnsignedShortLE();
                    onUnsignedShortValue(column, value);
                } else {
                    short value = buffer.readShortLE();
                    onShortValue(column, value);
                }
                return;
            }

            case FIELD_TYPE_TINY: {
                if (unsigned) {
                    short value = buffer.readUnsignedByte();
                    onUnsignedTinyValue(column, value);
                } else {
                    byte value = buffer.readByte();
                    onTinyValue(column, value);
                }
                return;
            }

            case FIELD_TYPE_DOUBLE: {
                double value = buffer.readDoubleLE();
                onDoubleValue(column, value);
                return;
            }

            case FIELD_TYPE_FLOAT: {
                float value = buffer.readFloatLE();
                onFloatValue(column, value);
                return;
            }

            case FIELD_TYPE_YEAR: {
                int value;
                if (unsigned) {
                    value = buffer.readUnsignedShortLE();
                } else {
                    value = buffer.readShortLE();
                }
                onYearValue(column, value);
                return;
            }

            case FIELD_TYPE_DECIMAL:
            case FIELD_TYPE_NEW_DECIMAL: {
                String valueStr = readLengthEncodedString(buffer, StandardCharsets.UTF_8);
                onDecimalValue(column, valueStr);
                return;
            }

            case FIELD_TYPE_DATETIME:
            case FIELD_TYPE_TIMESTAMP:
            case FIELD_TYPE_DATE: {
                decodeBinaryDatetime(column, buffer);
                return;
            }

            case FIELD_TYPE_TIME: {
                decodeBinaryTime(column, buffer);
                return;
            }

            case FIELD_TYPE_NULL: {
                nullValue(column);
                return;
            }

            case FIELD_TYPE_JSON: {
                int length = (int)readBinaryLength(buffer);
                onJsonValue(column, buffer, length);
                return;
            }

            case FIELD_TYPE_GEOMETRY: {
                int length = (int) readBinaryLength(buffer);
                onGeometryValue(column, buffer, length);
                return;
            }
        }

        throw new UnsupportedOperationException("Unsupported field type " + column.getDataType());
    }

    @Override
    public void textValue(MyDbColumn column, ByteBuf buffer, int length) {
        boolean unsigned = column.isUnsigned();

        switch (column.getDataType()) {
            case FIELD_TYPE_DATE: {
                decodeStringDate(column, buffer, length);
                return;
            }

            case FIELD_TYPE_DATETIME: {
                decodeStringDateTime(column, buffer, length, false);
                return;
            }

            case FIELD_TYPE_TIMESTAMP: {
                decodeStringDateTime(column, buffer, length, true);
                return;
            }

            case FIELD_TYPE_TIME: {
                decodeStringTime(column, buffer, length);
                return;
            }

            case FIELD_TYPE_YEAR: {
                int value = readBytesIntoInt(buffer, length);
                onYearValue(column, value);
                return;
            }

            case FIELD_TYPE_DECIMAL:
            case FIELD_TYPE_NEW_DECIMAL: {
                String value = ByteBufUtils.readFixedString(buffer, length, StandardCharsets.UTF_8);
                onDecimalValue(column, value);
                return;
            }

            case FIELD_TYPE_DOUBLE: {
                String value = ByteBufUtils.readFixedString(buffer, length, StandardCharsets.UTF_8);
                onDoubleValue(column, Double.parseDouble(value));
                return;
            }

            case FIELD_TYPE_FLOAT: {
                String value = ByteBufUtils.readFixedString(buffer, length, StandardCharsets.UTF_8);
                onFloatValue(column, Float.parseFloat(value));
                return;
            }

            case FIELD_TYPE_INT24: {
                int value = readBytesIntoInt(buffer, length);
                if (unsigned) {
                    onUnsignedInt24Value(column, value);
                } else {
                    onInt24Value(column, value);
                }
                return;
            }

            case FIELD_TYPE_LONG: {
                if (unsigned) {
                    long value = readBytesIntoLong(buffer, length);
                    onUnsignedLongValue(column, value);
                } else {
                    int value = readBytesIntoInt(buffer, length);
                    onLongValue(column, value);
                }
                return;
            }

            case FIELD_TYPE_LONGLONG: {
                if (unsigned) {
                    long value = readBytesIntoUnsignedLong(buffer, length);
                    onUnsignedLongLongValue(column, value);
                } else {
                    long value = readBytesIntoLong(buffer, length);
                    onLongLongValue(column, value);
                }
                return;
            }

            case FIELD_TYPE_SHORT: {
                int value = readBytesIntoInt(buffer, length);
                if (unsigned) {
                    onUnsignedShortValue(column, value);
                } else {
                    onShortValue(column, (short) value);
                }
                return;
            }

            case FIELD_TYPE_TINY: {
                int value = readBytesIntoInt(buffer, length);

                if (unsigned) {
                    onUnsignedTinyValue(column, (short) value);
                } else {
                    onTinyValue(column, (byte) value);
                }
                return;
            }

            case FIELD_TYPE_BLOB:
            case FIELD_TYPE_LONG_BLOB:
            case FIELD_TYPE_MEDIUM_BLOB:
            case FIELD_TYPE_TINY_BLOB:
            case FIELD_TYPE_VAR_STRING:
            case FIELD_TYPE_STRING: {
                if (column.getCharsetId() == CHARSET_ID_BINARY) {
                    onBlobValue(column, buffer, length);
                } else {
                    String value = ByteBufUtils.readFixedString(buffer, length, UTF_8);
                    onTextValue(column, value);
                }
                return;
            }

            case FIELD_TYPE_VARCHAR:
            case FIELD_TYPE_ENUM:
            case FIELD_TYPE_SET: {
                String value = ByteBufUtils.readFixedString(buffer, length, UTF_8);
                onTextValue(column, value);
                return;
            }

            case FIELD_TYPE_BIT: {
                onBlobValue(column, buffer, length);
                return;
            }

            case FIELD_TYPE_JSON: {
                onJsonValue(column, buffer, length);
                return;
            }

            case FIELD_TYPE_GEOMETRY: {
                onGeometryValue(column, buffer, length);
                return;
            }

            default:
                throw new ProtocolException("Unsupported field type - column.columnType");
        }
    }

    protected void decodeStringDate(MyDbColumn column, ByteBuf buffer, int length) {
        if (length != 10)
            throw new DecoderException("Expected a date column to have length 10");

        int pos = buffer.readerIndex();
        int yearHigh = getTwoAsciiDigits(buffer, pos    );
        int yearLow  = getTwoAsciiDigits(buffer, pos + 2);
        int month    = getTwoAsciiDigits(buffer, pos + 5);
        int day      = getTwoAsciiDigits(buffer, pos + 8);
        buffer.readerIndex(pos + 10);

        onDateValue(column, yearHigh * 100 + yearLow, month, day);
    }

    protected void decodeStringDateTime(MyDbColumn column, ByteBuf buffer, int length, boolean isTimestamp) {
        if (length < 19)
            throw new DecoderException("Expected a datetime/timestamp column to have length at least 19");

        int pos = buffer.readerIndex();

        int yearHigh = getTwoAsciiDigits(buffer, pos);
        int yearLow = getTwoAsciiDigits(buffer, pos + 2);
        int month = getTwoAsciiDigits(buffer, pos + 5);
        int day = getTwoAsciiDigits(buffer, pos + 8);
        int hour = getTwoAsciiDigits(buffer, pos + 11);
        int minute = getTwoAsciiDigits(buffer, pos + 14);
        int second = getTwoAsciiDigits(buffer, pos + 17);

        int micro;
        switch (length) {
            case 21:
                micro = getAsciiDigit(buffer, pos + 20) * 100000;
                break;

            case 22:
                micro = getTwoAsciiDigits(buffer, pos + 20) * 10000;
                break;

            case 23:
                micro = getTwoAsciiDigits(buffer, pos + 20) * 10000 +
                         getAsciiDigit(buffer, pos + 22) * 1000;
                break;

            case 24:
                micro = getTwoAsciiDigits(buffer, pos + 20) * 10000 +
                         getTwoAsciiDigits(buffer, pos + 22) * 100;
                break;

            case 25:
                micro = getTwoAsciiDigits(buffer, pos + 20) * 10000 +
                         getTwoAsciiDigits(buffer, pos + 22) * 100 +
                         getAsciiDigit(buffer, pos + 24) * 10;
                break;

            case 26:
                micro = getTwoAsciiDigits(buffer, pos + 20) * 10000 +
                         getTwoAsciiDigits(buffer, pos + 22) * 100 +
                         getTwoAsciiDigits(buffer, pos + 24);
                break;

            default:
                micro = 0;
                break;
        }

        buffer.readerIndex(pos + length);

        if (isTimestamp) {
            onTimestampValue(column, yearHigh * 100 + yearLow, month, day, hour, minute, second, micro);
        } else {
            onDateTimeValue(column, yearHigh * 100 + yearLow, month, day, hour, minute, second, micro);
        }
    }

    protected void decodeStringTime(MyDbColumn column, ByteBuf buffer, int length) {
        if (length < 8 || length > 17) // 12:34:56, -838:59:59.000000..838:59:59.000000
            throw new DecoderException("Expected a time column to have length at least 8");

        int pos = buffer.readerIndex();
        boolean negative = false;
        if (buffer.getByte(pos) == '-') {
            negative = true;
            pos++;
            length--;
        }

        int hour = getTwoAsciiDigits(buffer, pos);
        if (buffer.getByte(pos + 2) != ':') {
            hour = hour * 10 + getAsciiDigit(buffer, pos++ + 2);
            length--;
        }

        int minute = getTwoAsciiDigits(buffer, pos + 3);
        int second = getTwoAsciiDigits(buffer, pos + 6);

        int micro;
        switch (length) {
            case 10:
                micro = getAsciiDigit(buffer, pos + 9) * 100000;
                break;

            case 11:
                micro = getTwoAsciiDigits(buffer, pos + 9) * 10000;
                break;

            case 12:
                micro = getTwoAsciiDigits(buffer, pos + 9) * 10000 +
                        getAsciiDigit(buffer, pos + 11) * 1000;
                break;

            case 13:
                micro = getTwoAsciiDigits(buffer, pos + 9) * 10000 +
                        getTwoAsciiDigits(buffer, pos + 11) * 100;
                break;

            case 14:
                micro = getTwoAsciiDigits(buffer, pos + 9) * 10000 +
                        getTwoAsciiDigits(buffer, pos + 11) * 100 +
                        getAsciiDigit(buffer, pos + 13) * 10;
                break;

            case 15:
                micro = getTwoAsciiDigits(buffer, pos + 9) * 10000 +
                        getTwoAsciiDigits(buffer, pos + 11) * 100 +
                        getTwoAsciiDigits(buffer, pos + 13);
                break;

            default:
                micro = 0;
                break;
        }

        buffer.readerIndex(pos + length);

        onTimeValue(column, negative, hour, minute, second, micro);
    }

    protected void decodeBinaryDatetime(MyDbColumn column, ByteBuf buffer) {
        int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0, micro = 0;

        int size = buffer.readUnsignedByte();
        if (size != 0 && size != 4 && size != 7 && size != 11)
            throw new DecodingException("Unexpected length for a timestamp (" + size + ")");

        if (size >= 4) {
            year = buffer.readUnsignedShortLE();
            month = buffer.readUnsignedByte();
            day = buffer.readUnsignedByte();
        }
        if (size >= 7) {
            hour = buffer.readUnsignedByte();
            minute = buffer.readUnsignedByte();
            second = buffer.readUnsignedByte();
        }
        if (size >= 11) {
            micro = (int)(buffer.readUnsignedIntLE());
        }

        switch (column.getDataType()) {
            case FIELD_TYPE_DATE:
                onDateValue(column, year, month, day);
                return;

            case FIELD_TYPE_DATETIME:
                onDateTimeValue(column, year, month, day, hour, minute, second, micro);
                return;

            case FIELD_TYPE_TIMESTAMP:
                onTimestampValue(column, year, month, day, hour, minute, second, micro);
                return;
        }

        throw new AssertionError("Should be unreachable - column type was " + column.getDataType());
    }

    protected void decodeBinaryTime(MyDbColumn column, ByteBuf buffer) {
        int len = buffer.readUnsignedByte();

        switch (len) {
            case 0: {
                onTimeValue(column, false, 0, 0, 0, 0);
                return;
            }

            case 8: {
                boolean negative = buffer.readUnsignedByte() == 1;

                int days = (int)buffer.readUnsignedIntLE();
                int hours = buffer.readUnsignedByte();
                int minutes = buffer.readUnsignedByte();
                int seconds = buffer.readUnsignedByte();

                onTimeValue(column, negative, days * 24 + hours, minutes, seconds, 0);
                return;
            }

            case 12: {
                boolean negative = buffer.readUnsignedByte() == 1;

                int days = (int)buffer.readUnsignedIntLE();
                int hours = buffer.readUnsignedByte();
                int minutes = buffer.readUnsignedByte();
                int seconds = buffer.readUnsignedByte();
                int micros = (int)(buffer.readUnsignedIntLE());

                onTimeValue(column, negative, days * 24 + hours, minutes, seconds, micros);
                return;
            }

            default:
                throw new DecodingException("Unexpected length for a duration (" + len + ")");
        }
    }

    private static int readBytesIntoInt(ByteBuf buffer, int length) {
        int remain = length;
        if (remain < 1)
            return 0;

        boolean negative = false;
        if (buffer.getByte(buffer.readerIndex()) == '-') {
            negative = true;
            buffer.readByte();
            remain--;
        }

        int value = 0;
        while (remain-- > 0)
            value = value * 10 - (buffer.readByte() - '0'); // accumulate negatively, to be able to read Integer.MIN_VALUE

        return negative ? value : -value;
    }

    private static long readBytesIntoLong(ByteBuf buffer, int length) {
        if (length <= 9)
            return readBytesIntoInt(buffer, length);

        int remain = length;

        boolean negative = false;
        if (buffer.getByte(buffer.readerIndex()) == '-') {
            negative = true;
            buffer.readByte();
            remain--;
        }

        long value = 0;
        while (remain-- > 0)
            value = value * 10 - (buffer.readByte() - '0');

        return negative ? value : -value;
    }

    private static long readBytesIntoUnsignedLong(ByteBuf packet, int length) {
        if (length < 19)
            return readBytesIntoLong(packet, length);

        // TODO - avoid String creation
        String str = ByteBufUtils.readFixedString(packet, length, StandardCharsets.UTF_8);
        return Long.parseUnsignedLong(str);
    }
}
