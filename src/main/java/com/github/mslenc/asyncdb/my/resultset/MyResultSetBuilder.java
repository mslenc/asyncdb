package com.github.mslenc.asyncdb.my.resultset;

import com.github.mslenc.asyncdb.my.MyDbColumn;
import io.netty.buffer.ByteBuf;

public interface MyResultSetBuilder<QR> {
    void startRow();

    void nullValue(MyDbColumn column);
    void binaryValue(MyDbColumn column, ByteBuf buffer);
    void textValue(MyDbColumn column, ByteBuf buffer, int length);

    void endRow();

    QR build(int statusFlags, int warnings);
}
