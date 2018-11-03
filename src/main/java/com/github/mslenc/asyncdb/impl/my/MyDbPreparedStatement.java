package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.my.MyPreparedStatement;

import java.util.List;
import java.util.concurrent.CompletableFuture;


class MyDbPreparedStatement implements DbPreparedStatement {
    private final MyPreparedStatement ps;

    MyDbPreparedStatement(MyPreparedStatement ps) {
        this.ps = ps;
    }

    @Override
    public DbColumns getColumns() {
        return ps.getColumns();
    }

    @Override
    public DbColumns getParameters() {
        return ps.getParameters();
    }

    @Override
    public CompletableFuture<DbExecResult> execute(List<Object> values) {
        return ps.execute(values, MyDbQueryResultBuilderFactory.instance);
    }

    @Override
    public void streamQuery(DbQueryResultObserver streamHandler, List<Object> values) {
        CompletableFuture<Void> future = ps.execute(values, new MyStreamingRSBFactory(streamHandler));
        MyDbConnection.forwardError(future, streamHandler);
    }

    @Override
    public CompletableFuture<Void> close() {
        return ps.close();
    }
}