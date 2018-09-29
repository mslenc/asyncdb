package com.github.mslenc.asyncdb.mysql.codec;

import com.github.mslenc.asyncdb.common.column.*;
import com.github.mslenc.asyncdb.common.column.ByteDecoder;
import com.github.mslenc.asyncdb.mysql.binary.decoder.*;
import com.github.mslenc.asyncdb.mysql.column.ByteArrayColumnDecoder;
import com.github.mslenc.asyncdb.mysql.column.DurationDecoder;

import java.time.OffsetDateTime;

import static com.github.mslenc.asyncdb.mysql.column.ColumnType.*;
import static com.github.mslenc.asyncdb.mysql.util.MySQLIO.CHARSET_ID_BINARY;
import static com.github.mslenc.asyncdb.mysql.util.MySQLIO.FIELD_FLAG_UNSIGNED;

public class DecoderRegistry {
    private static final DecoderRegistry instance = new DecoderRegistry();

    public static DecoderRegistry instance() {
        return instance;
    }

    public BinaryDecoder binaryDecoderFor(int columnType, int charsetCode, int flags) {
        boolean unsigned = (flags & FIELD_FLAG_UNSIGNED) != 0;

        switch (columnType) {
            case FIELD_TYPE_VARCHAR:
            case FIELD_TYPE_ENUM:
            case FIELD_TYPE_SET:
                return StringDecoder.instance();

            case FIELD_TYPE_BLOB:
            case FIELD_TYPE_LONG_BLOB:
            case FIELD_TYPE_MEDIUM_BLOB:
            case FIELD_TYPE_TINY_BLOB:
            case FIELD_TYPE_VAR_STRING:
            case FIELD_TYPE_STRING:
                if (charsetCode == CHARSET_ID_BINARY) {
                    return ByteArrayDecoder.instance();
                } else {
                    return StringDecoder.instance();
                }

            case FIELD_TYPE_BIT:
                return ByteArrayDecoder.instance();

            case FIELD_TYPE_LONGLONG:
                return LongDecoder.instance(unsigned);

            case FIELD_TYPE_LONG:
                return IntegerDecoder.instance(unsigned);

            case FIELD_TYPE_INT24:
                return IntegerDecoder.instance(false);

            case FIELD_TYPE_SHORT:
                return ShortDecoder.instance(unsigned);

            case FIELD_TYPE_TINY:
                return com.github.mslenc.asyncdb.mysql.binary.decoder.ByteDecoder.instance(unsigned);

            case FIELD_TYPE_DOUBLE:
                return DoubleDecoder.instance();

            case FIELD_TYPE_FLOAT:
                return FloatDecoder.instance();

            case FIELD_TYPE_YEAR:
                return YearDecoder.instance();

            case FIELD_TYPE_DECIMAL:
            case FIELD_TYPE_NEW_DECIMAL:
                return BigDecimalDecoder.instance();

            case FIELD_TYPE_DATETIME:
                return DateTimeDecoder.instance();

            case FIELD_TYPE_TIMESTAMP:
                return InstantDecoder.instance();

            case FIELD_TYPE_DATE:
                return DateDecoder.instance();

            case FIELD_TYPE_TIME:
                return TimeDecoder.instance();

            case FIELD_TYPE_NULL:
                return NullDecoder.instance();

            case FIELD_TYPE_JSON:
                // TODO: verify if this is appropriate for JSON
                return StringDecoder.instance();

            case FIELD_TYPE_GEOMETRY:
                // TODO: verify if this is appropriate for GEOMETRY
                return ByteArrayDecoder.instance();

            default:
                throw new IllegalStateException("Missing decoder for columnType " + columnType);
        }
    }

    public ColumnDecoder textDecoderFor(int columnType, int charsetCode, int flags) {
        boolean unsigned = (flags & FIELD_FLAG_UNSIGNED) != 0;

        switch (columnType) {
            case FIELD_TYPE_DATE:
                return DateEncoderDecoder.instance();

            case FIELD_TYPE_DATETIME:
                return LocalDateTimeEncoderDecoder.instance();

            case FIELD_TYPE_TIMESTAMP:
                return InstantEncoderDecoder.instance();

            case FIELD_TYPE_DECIMAL:
            case FIELD_TYPE_NEW_DECIMAL:
                return BigDecimalEncoderDecoder.instance();

            case FIELD_TYPE_DOUBLE:
                return DoubleEncoderDecoder.instance();

            case FIELD_TYPE_FLOAT:
                return FloatEncoderDecoder.instance();

            case FIELD_TYPE_INT24:
                return IntegerEncoderDecoder.instance();

            case FIELD_TYPE_LONG:
                if (unsigned) {
                    return LongEncoderDecoder.instance();
                } else {
                    return IntegerEncoderDecoder.instance();
                }

            case FIELD_TYPE_LONGLONG:
                if (unsigned) {
                    return UnsignedLongEncoderDecoder.instance();
                } else {
                    return LongEncoderDecoder.instance();
                }

            case FIELD_TYPE_SHORT:
                if (unsigned) {
                    return IntegerEncoderDecoder.instance();
                } else {
                    return ShortEncoderDecoder.instance();
                }

            case FIELD_TYPE_TIME:
                return DurationDecoder.instance();

            case FIELD_TYPE_TINY:
                if (unsigned) {
                    return ShortEncoderDecoder.instance();
                } else {
                    return ByteDecoder.instance();
                }

            case FIELD_TYPE_VARCHAR:
            case FIELD_TYPE_ENUM:
                return StringEncoderDecoder.instance();

            case FIELD_TYPE_YEAR:
                return YearEncoderDecoder.instance();

            case FIELD_TYPE_BIT:
                return ByteArrayColumnDecoder.instance();

            case FIELD_TYPE_BLOB:
            case FIELD_TYPE_VAR_STRING:
            case FIELD_TYPE_STRING: {
                if (charsetCode == CHARSET_ID_BINARY) {
                    return ByteArrayColumnDecoder.instance();
                } else {
                    return StringEncoderDecoder.instance();
                }
            }

            default:
                return StringEncoderDecoder.instance();
        }
    }
}