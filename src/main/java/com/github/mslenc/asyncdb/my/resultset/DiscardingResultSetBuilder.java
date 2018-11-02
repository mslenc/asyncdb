package com.github.mslenc.asyncdb.my.resultset;

import com.github.mslenc.asyncdb.my.MyDbColumn;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import io.netty.buffer.ByteBuf;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.*;
import static com.github.mslenc.asyncdb.my.MyConstants.*;

public class DiscardingResultSetBuilder implements MyResultSetBuilder<Void> {
    public static final DiscardingResultSetBuilder instance = new DiscardingResultSetBuilder();

    @Override
    public void startRow() {

    }

    @Override
    public void endRow() {

    }

    @Override
    public Void build(int statusFlags, int warnings) {
        return null;
    }

    @Override
    public void nullValue(MyDbColumn column) {

    }

    @Override
    public void textValue(MyDbColumn column, ByteBuf buffer, int length) {
        buffer.skipBytes(length);
    }

    @Override
    public void binaryValue(MyDbColumn column, ByteBuf buffer) {
        switch (column.getDataType()) {
            case FIELD_TYPE_GEOMETRY:
            case FIELD_TYPE_JSON:
            case FIELD_TYPE_DECIMAL:
            case FIELD_TYPE_NEW_DECIMAL:
            case FIELD_TYPE_VARCHAR:
            case FIELD_TYPE_ENUM:
            case FIELD_TYPE_SET:
            case FIELD_TYPE_BIT:
            case FIELD_TYPE_TINY_BLOB:
            case FIELD_TYPE_MEDIUM_BLOB:
            case FIELD_TYPE_LONG_BLOB:
            case FIELD_TYPE_BLOB:
            case FIELD_TYPE_STRING:
            case FIELD_TYPE_VAR_STRING: {
                skipLengthEncodedString(buffer);
                return;
            }

            case FIELD_TYPE_DOUBLE:
            case FIELD_TYPE_LONGLONG: {
                buffer.skipBytes(8);
                return;
            }

            case FIELD_TYPE_FLOAT:
            case FIELD_TYPE_INT24:
            case FIELD_TYPE_LONG: {
                buffer.skipBytes(4);
                return;
            }

            case FIELD_TYPE_YEAR:
            case FIELD_TYPE_SHORT: {
                buffer.skipBytes(2);
                return;
            }

            case FIELD_TYPE_TINY: {
                buffer.skipBytes(1);
                return;
            }

            case FIELD_TYPE_TIME:
            case FIELD_TYPE_DATETIME:
            case FIELD_TYPE_TIMESTAMP:
            case FIELD_TYPE_DATE: {
                buffer.skipBytes(buffer.readUnsignedByte());
                return;
            }

            case FIELD_TYPE_NULL: {
                return;
            }
        }

        throw new UnsupportedOperationException("Unsupported field type " + column.getDataType());
    }

    public static class Factory implements MyResultSetBuilderFactory<Void> {
        public static final Factory instance = new Factory();

        @Override
        public Void makeQueryResultWithNoRows(long rowsAffected, String message, long lastInsertId, int statusFlags, int warnings) {
            return null;
        }

        @Override
        public MyResultSetBuilder<Void> makeResultSetBuilder(MyEncoders encoders, MyDbColumns columns) {
            return DiscardingResultSetBuilder.instance;
        }
    }
}
