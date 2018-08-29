package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.sql.Date;

public class SQLDateEncoder implements BinaryEncoder {
    private static final SQLDateEncoder instance = new SQLDateEncoder();

    public static SQLDateEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        Date date = (Date)value;
        LocalDateEncoder.instance().encode(date.toLocalDate(), buffer);
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_DATE;
    }
}

