package com.github.mslenc.asyncdb.mysql.codec;

import com.github.mslenc.asyncdb.common.PreparedStatement;
import com.github.mslenc.asyncdb.common.QueryResult;
import com.github.mslenc.asyncdb.common.exceptions.DatabaseException;
import com.github.mslenc.asyncdb.common.util.FutureUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.emptyList;

class PreparedStatementImpl implements PreparedStatement {
    private final MySQLConnectionHandler conn;
    private final String query;
    private final PreparedStatementInfo psInfo;
    private CompletableFuture<Void> closingPromise;

    public PreparedStatementImpl(MySQLConnectionHandler conn, String query, PreparedStatementInfo psInfo) {
        this.conn = conn;
        this.query = query;
        this.psInfo = psInfo;
    }

    @Override
    public int getNumberOfColumns() {
        return psInfo.columnDefs.size();
    }

    @Override
    public int getNumberOfParameters() {
        return psInfo.paramDefs.size();
    }

    @Override
    public CompletableFuture<QueryResult> execute(List<Object> values) {
        int numValues = values != null ? values.size() : 0;
        if (numValues != psInfo.paramDefs.size())
            return FutureUtils.failedFuture(new IllegalArgumentException("Size of values (" + numValues + ") does not match parameters count of the prepared statement ("+ psInfo.paramDefs.size() + ")"));

        if (closingPromise != null)
            return FutureUtils.failedFuture(new DatabaseException("This prepared statement is already closed."));

        if (values == null)
            values = emptyList();

        return conn.executePreparedStatement(psInfo, values);
    }

    @Override
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
