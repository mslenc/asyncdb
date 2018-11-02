package com.github.mslenc.asyncdb.my;

import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.ex.DatabaseException;
import com.github.mslenc.asyncdb.util.FutureUtils;
import com.github.mslenc.asyncdb.my.io.PreparedStatementInfo;
import com.github.mslenc.asyncdb.my.resultset.MyResultSetBuilderFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.emptyList;

public class MyPreparedStatement {
    private final MyConnection conn;
    private final String query;
    private final PreparedStatementInfo psInfo;
    private CompletableFuture<Void> closingPromise;

    public MyPreparedStatement(MyConnection conn, String query, PreparedStatementInfo psInfo) {
        this.conn = conn;
        this.query = query;
        this.psInfo = psInfo;
    }

    public DbColumns getColumns() {
        return psInfo.getColumns();
    }

    public DbColumns getParameters() {
        return psInfo.getParameters();
    }

    public int getNumberOfColumns() {
        return psInfo.columnDefs.size();
    }

    public int getNumberOfParameters() {
        return psInfo.paramDefs.size();
    }

    public <QR> CompletableFuture<QR> execute(List<Object> values, MyResultSetBuilderFactory<QR> rsFactory) {
        int numValues = values != null ? values.size() : 0;
        if (numValues != psInfo.paramDefs.size())
            return FutureUtils.failedFuture(new IllegalArgumentException("Size of values (" + numValues + ") does not match parameters count of the prepared statement ("+ psInfo.paramDefs.size() + ")"));

        if (closingPromise != null)
            return FutureUtils.failedFuture(new DatabaseException("This prepared statement is already closed."));

        if (values == null)
            values = emptyList();

        return conn.executePreparedStatement(psInfo, values, rsFactory);
    }

    public CompletableFuture<Void> close() {
        if (closingPromise != null)
            return closingPromise;

        this.closingPromise = new CompletableFuture<>();
        conn.closePreparedStatement(psInfo).whenComplete((closeSuccess, closeError) -> {
            if (closeError != null) {
                closingPromise.completeExceptionally(closeError);
            } else {
                closingPromise.complete(closeSuccess);
            }
        });
        return closingPromise;
    }
}
