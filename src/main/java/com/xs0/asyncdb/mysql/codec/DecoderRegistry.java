package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.common.column.*;
import com.xs0.asyncdb.mysql.binary.decoder.*;
import com.xs0.asyncdb.mysql.binary.decoder.ByteDecoder;
import com.xs0.asyncdb.mysql.column.ByteArrayColumnDecoder;
import com.xs0.asyncdb.mysql.column.DurationDecoder;

import static com.xs0.asyncdb.mysql.column.ColumnType.*;
import static com.xs0.asyncdb.mysql.util.CharsetMapper.CHARSET_BINARY;

public class DecoderRegistry {
    private static final DecoderRegistry instance = new DecoderRegistry();

    public static DecoderRegistry instance() {
        return instance;
    }

    public BinaryDecoder binaryDecoderFor(int columnType, int charsetCode) {
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
                if (charsetCode == CHARSET_BINARY) {
                    return ByteArrayDecoder.instance();
                } else {
                    return StringDecoder.instance();
                }

            case FIELD_TYPE_BIT:
                return ByteArrayDecoder.instance();

            case FIELD_TYPE_LONGLONG:
                return LongDecoder.instance();

            case FIELD_TYPE_LONG:
            case FIELD_TYPE_INT24:
                return IntegerDecoder.instance();

            case FIELD_TYPE_YEAR:
                return YearDecoder.instance();

            case FIELD_TYPE_SHORT:
                return ShortDecoder.instance();

            case FIELD_TYPE_TINY:
                return ByteDecoder.instance();

            case FIELD_TYPE_DOUBLE:
                return DoubleDecoder.instance();

            case FIELD_TYPE_FLOAT:
                return FloatDecoder.instance();

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

    public ColumnDecoder textDecoderFor(int columnType, int charsetCode) {
        switch (columnType) {
            case FIELD_TYPE_DATE:
                return DateEncoderDecoder.instance();

            case FIELD_TYPE_DATETIME:
            case FIELD_TYPE_TIMESTAMP:
                return LocalDateTimeEncoderDecoder.instance();

            case FIELD_TYPE_DECIMAL:
            case FIELD_TYPE_NEW_DECIMAL:
                return BigDecimalEncoderDecoder.instance();

            case FIELD_TYPE_DOUBLE:
                return DoubleEncoderDecoder.instance();

            case FIELD_TYPE_FLOAT:
                return FloatEncoderDecoder.instance();

            case FIELD_TYPE_INT24:
            case FIELD_TYPE_LONG:
                return IntegerEncoderDecoder.instance();

            case FIELD_TYPE_LONGLONG:
                return LongEncoderDecoder.instance();

            case FIELD_TYPE_SHORT:
                return ShortEncoderDecoder.instance();

            case FIELD_TYPE_TIME:
                return DurationDecoder.instance();

            case FIELD_TYPE_TINY:
                return com.xs0.asyncdb.common.column.ByteDecoder.instance();

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
                if (charsetCode == CHARSET_BINARY) {
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
