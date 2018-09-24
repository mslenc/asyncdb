package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.column.ColumnType;
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
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_DATE;
    }
}

