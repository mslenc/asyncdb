package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.impl.DbRowImpl;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.my.resultset.MyDbRowResultSetBuilder;

class MyStreamingRSBuilder extends MyDbRowResultSetBuilder<Void> {
    private final DbQueryResultObserver streamHandler;

    MyStreamingRSBuilder(DbQueryResultObserver streamHandler, MyEncoders encoders, MyDbColumns columns) {
        super(encoders, columns);
        this.streamHandler = streamHandler;
    }

    @Override
    protected void onRow(DbRowImpl row) {
        streamHandler.onNext(row);
    }

    @Override
    public Void build(int statusFlags, int warnings) {
        streamHandler.onCompleted();
        return null;
    }
}