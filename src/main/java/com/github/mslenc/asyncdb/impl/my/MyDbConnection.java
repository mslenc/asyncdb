package com.github.mslenc.asyncdb.impl.my;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.ex.ConnectionClosedException;
import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.resultset.DiscardingResultSetBuilder;
import com.github.mslenc.asyncdb.util.SqlQueryPlaceholders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.util.FutureUtils.failedFuture;
import static com.github.mslenc.asyncdb.util.FutureUtils.safelyFail;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;


public class MyDbConnection implements DbConnection {
    private MyConnection myConn;
    private Runnable onSuccessfulClose;

    MyDbConnection(MyConnection myConn, Runnable onSuccessfulClose) {
        this.myConn = requireNonNull(myConn);
        this.onSuccessfulClose = requireNonNull(onSuccessfulClose);
    }

    void runInitStatements(List<String> initStatements, CompletableFuture<DbConnection> promise) {
        runInitStatements(initStatements, 0, promise);
    }

    ByteBuf encodeUtf8(String sql) {
        return Unpooled.copiedBuffer(sql, UTF_8);
    }

    ByteBuf encodeUtf8WithValues(String sql, List<Object> values) {
        ByteBuf sqlWithValues = Unpooled.buffer(sql.length() + values.size() * 20 + 10);
        SqlQueryPlaceholders.insertValuesForPlaceholders(sql, values, myConn.encoders, sqlWithValues);
        return sqlWithValues;
    }

    private void runInitStatements(List<String> initStatements, int currentIndex, CompletableFuture<DbConnection> promise) {
        if (currentIndex >= initStatements.size()) {
            promise.complete(this);
            return;
        }

        MyConnection myConn = this.myConn;
        if (myConn == null) {
            safelyFail(promise, new ConnectionClosedException());
            return;
        }

        ByteBuf stmtBytes = encodeUtf8(initStatements.get(currentIndex));
        CompletableFuture<Void> future = myConn.sendQuery(stmtBytes, DiscardingResultSetBuilder.Factory.instance);
        future.whenComplete((result, error) -> {
            if (error != null) {
                try {
                    try {
                        promise.completeExceptionally(error);
                    } finally {
                        myConn.disconnect();
                    }
                } finally {
                    MyDbConnection.this.myConn = null;
                }
            } else {
                runInitStatements(initStatements, currentIndex + 1, promise);
            }
        });
    }

    @Override
    public CompletableFuture<DbQueryResult> sendQuery(String sql) {
        MyConnection myConn = this.myConn;
        if (myConn == null)
            return failedFuture(new ConnectionClosedException());

        ByteBuf sqlBytes = encodeUtf8(sql);
        return myConn.sendQuery(sqlBytes, MyDbQueryResultBuilderFactory.instance);
    }

    @Override
    public CompletableFuture<DbQueryResult> sendQuery(String sql, List<Object> values) {
        MyConnection myConn = this.myConn;
        if (myConn == null)
            return failedFuture(new ConnectionClosedException());

        ByteBuf sqlBytes;
        try {
            sqlBytes = encodeUtf8WithValues(sql, values);
        } catch (Exception e) {
            return failedFuture(e);
        }

        return myConn.sendQuery(sqlBytes, MyDbQueryResultBuilderFactory.instance);
    }

    @Override
    public void streamQuery(String sql, DbResultObserver streamHandler) {
        MyConnection myConn = this.myConn;
        if (myConn == null) {
            streamHandler.onError(new ConnectionClosedException());
            return;
        }

        CompletableFuture<Void> future = myConn.sendQuery(encodeUtf8(sql), new MyStreamingRSBFactory(streamHandler));
        forwardError(future, streamHandler);
    }

    @Override
    public void streamQuery(String sql, DbResultObserver streamHandler, List<Object> values) {
        MyConnection myConn = this.myConn;
        if (myConn == null) {
            streamHandler.onError(new ConnectionClosedException());
            return;
        }

        ByteBuf sqlBytes;
        try {
            sqlBytes = encodeUtf8WithValues(sql, values);
        } catch (Exception e) {
            streamHandler.onError(e);
            return;
        }

        CompletableFuture<Void> future = myConn.sendQuery(sqlBytes, new MyStreamingRSBFactory(streamHandler));
        forwardError(future, streamHandler);
    }

    @Override
    public CompletableFuture<DbPreparedStatement> prepareStatement(String sql) {
        MyConnection myConn = this.myConn;
        if (myConn == null)
            return failedFuture(new ConnectionClosedException());

        CompletableFuture<DbPreparedStatement> promise = new CompletableFuture<>();

        myConn.prepareStatement(sql).whenComplete((ps, error) -> {
            if (error != null) {
                promise.completeExceptionally(error);
            } else {
                promise.complete(new MyDbPreparedStatement(ps));
            }
        });

        return promise;
    }

    @Override
    public CompletableFuture<Void> close() {
        MyConnection myConn = this.myConn;
        if (myConn == null)
            return failedFuture(new ConnectionClosedException());

        this.myConn = null;
        return myConn.logicallyClose().whenComplete((result, error) -> {
            if (error != null) {
                myConn.disconnect();
            } else {
                onSuccessfulClose.run();
            }
        });
    }

    static void forwardError(CompletableFuture<Void> future, DbResultObserver streamHandler) {
        future.whenComplete((result, error) -> {
            if (error != null) {
                streamHandler.onError(error);
            }
        });
    }
}
