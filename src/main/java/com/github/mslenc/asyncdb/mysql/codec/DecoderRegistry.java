package com.github.mslenc.asyncdb.mysql.codec;

import com.github.mslenc.asyncdb.mysql.column.*;
import com.github.mslenc.asyncdb.mysql.binary.decoder.*;

import static com.github.mslenc.asyncdb.mysql.util.MySQLIO.*;

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

    public TextValueDecoder textDecoderFor(int columnType, int charsetCode, int flags) {
        boolean unsigned = (flags & FIELD_FLAG_UNSIGNED) != 0;

        switch (columnType) {
            case FIELD_TYPE_DATE:
                return LocalDateTextDecoder.instance();

            case FIELD_TYPE_DATETIME:
                return LocalDateTimeTextDecoder.instance();

            case FIELD_TYPE_TIMESTAMP:
                return InstantTextDecoder.instance();

            case FIELD_TYPE_DECIMAL:
            case FIELD_TYPE_NEW_DECIMAL:
                return BigDecimalTextDecoder.instance();

            case FIELD_TYPE_DOUBLE:
                return DoubleTextDecoder.instance();

            case FIELD_TYPE_FLOAT:
                return FloatTextDecoder.instance();

            case FIELD_TYPE_INT24:
                return IntegerTextDecoder.instance();

            case FIELD_TYPE_LONG:
                if (unsigned) {
                    return LongTextDecoder.instance();
                } else {
                    return IntegerTextDecoder.instance();
                }

            case FIELD_TYPE_LONGLONG:
                if (unsigned) {
                    return ULongTextDecoder.instance();
                } else {
                    return LongTextDecoder.instance();
                }

            case FIELD_TYPE_SHORT:
                if (unsigned) {
                    return IntegerTextDecoder.instance();
                } else {
                    return ShortTextDecoder.instance();
                }

            case FIELD_TYPE_TIME:
                return DurationTextDecoder.instance();

            case FIELD_TYPE_TINY:
                if (unsigned) {
                    return ShortTextDecoder.instance();
                } else {
                    return ByteTextDecoder.instance();
                }

            case FIELD_TYPE_VARCHAR:
            case FIELD_TYPE_ENUM:
            case FIELD_TYPE_SET:
                return StringTextDecoder.instance();

            case FIELD_TYPE_YEAR:
                return YearTextDecoder.instance();

            case FIELD_TYPE_BIT:
                return ByteArrayTextDecoder.instance();

            case FIELD_TYPE_BLOB:
            case FIELD_TYPE_VAR_STRING:
            case FIELD_TYPE_STRING: {
                if (charsetCode == CHARSET_ID_BINARY) {
                    return ByteArrayTextDecoder.instance();
                } else {
                    return StringTextDecoder.instance();
                }
            }

            default:
                return StringTextDecoder.instance();
        }
    }
}
