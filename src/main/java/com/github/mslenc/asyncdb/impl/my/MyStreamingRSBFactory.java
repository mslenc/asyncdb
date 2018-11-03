package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilder;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilderFactory;

class MyStreamingRSBFactory implements MyResultSetBuilderFactory<Void> {
    private final DbQueryResultObserver streamHandler;

    MyStreamingRSBFactory(DbQueryResultObserver streamHandler) {
        this.streamHandler = streamHandler;
    }

    @Override
    public MyResultSetBuilder<Void> makeResultSetBuilder(MyEncoders encoders, MyDbColumns columns) {
        return new MyStreamingRSBuilder(streamHandler, encoders, columns);
    }

    @Override
    public Void makeQueryResultWithNoRows(long rowsAffected, String message, long lastInsertId, int statusFlags, int warnings) {
        streamHandler.onCompleted();
        return null;
    }
}