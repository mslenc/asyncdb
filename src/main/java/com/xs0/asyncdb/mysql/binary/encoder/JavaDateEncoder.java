package com.xs0.asyncdb.mysql.binary.encoder;

import com.xs0.asyncdb.mysql.column.ColumnType;
import io.netty.buffer.ByteBuf;

import java.time.ZoneId;
import java.util.Date;

public class JavaDateEncoder implements BinaryEncoder {
    @Override
    public void encode(Object value, ByteBuf buffer) {
        Date date = (Date) value;
        LocalDateTimeEncoder.instance().encode(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), buffer);
        // TODO: handle zone appropriately
    }

    @Override
    public ColumnType encodesTo() {
        return ColumnType.FIELD_TYPE_TIMESTAMP;
    }
}
