package com.github.mslenc.asyncdb.my.resultset;

import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;

public interface MyResultSetBuilderFactory<QR> {
    MyResultSetBuilder<QR> makeResultSetBuilder(MyEncoders encoders, MyDbColumns columns);
    QR makeQueryResultWithNoRows(long rowsAffected, String message, long lastInsertId, int statusFlags, int warnings);
}
