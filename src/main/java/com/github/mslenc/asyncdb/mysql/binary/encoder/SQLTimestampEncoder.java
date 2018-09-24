package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.sql.Timestamp;

public class SQLTimestampEncoder implements BinaryEncoder {
    private static final SQLTimestampEncoder instance = new SQLTimestampEncoder();

    public static SQLTimestampEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        Timestamp date = (Timestamp) value;
        LocalDateTimeEncoder.instance().encode(date.toLocalDateTime(), buffer);
        // TODO: timezone handling
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TIMESTAMP;
    }
}
