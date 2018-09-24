package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.sql.Time;

public class SQLTimeEncoder implements BinaryEncoder {
    private static final SQLTimeEncoder instance = new SQLTimeEncoder();

    public static SQLTimeEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        Time time = (Time) value;
        LocalTimeEncoder.instance().encode(time.toLocalTime(), buffer);
        // TODO: timezone handling
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TIME;
    }
}
