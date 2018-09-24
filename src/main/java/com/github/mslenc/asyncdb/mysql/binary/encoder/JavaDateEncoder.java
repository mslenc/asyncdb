package com.github.mslenc.asyncdb.mysql.binary.encoder;

import com.github.mslenc.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.ZoneId;
import java.util.Date;

public class JavaDateEncoder implements BinaryEncoder {
    private static final JavaDateEncoder instance = new JavaDateEncoder();

    public static JavaDateEncoder instance() {
        return instance;
    }

    @Override
    public void encode(Object value, ByteBuf buffer) {
        Date date = (Date) value;
        LocalDateTimeEncoder.instance().encode(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), buffer);
        // TODO: handle zone appropriately
    }

    @Override
    public int encodesTo() {
        return ColumnType.FIELD_TYPE_TIMESTAMP;
    }
}
