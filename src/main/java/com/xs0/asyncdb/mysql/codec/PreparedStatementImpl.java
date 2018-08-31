package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.common.PreparedStatement;
import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.common.exceptions.DatabaseException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.xs0.asyncdb.common.util.FutureUtils.failedFuture;
import static java.util.Collections.emptyList;

class PreparedStatementImpl implements PreparedStatement {
    final MySQLConnectionHandler conn;
    final String query;
    final PreparedStatementInfo psInfo;
    CompletableFuture<Void> closingPromise;

    public PreparedStatementImpl(MySQLConnectionHandler conn, String query, PreparedStatementInfo psInfo) {
        this.conn = conn;
        this.query = query;
        this.psInfo = psInfo;
    }

    @Override
    public CompletableFuture<QueryResult> execute(List<Object> values) {
        int numValues = values != null ? values.size() : 0;
        if (numValues != psInfo.paramDefs.size())
            return failedFuture(new IllegalArgumentException("Size of values (" + numValues + ") does not match parameters count of the prepared statement ("+ psInfo.paramDefs.size() + ")"));

        if (closingPromise != null)
            return failedFuture(new DatabaseException("This prepared statement is already closed."));

        if (values == null)
            values = emptyList();

        return conn.executePreparedStatement(psInfo, values);
    }

    @Override
    public CompletableFuture<Void> close() {
        if (closingPromise != null)
            return closingPromise;

        CompletableFuture<Void> promise = new CompletableFuture<>();
        this.closingPromise = promise;
        conn.closePreparedStatement(psInfo, closingPromise);
        return promise;
    }
}
