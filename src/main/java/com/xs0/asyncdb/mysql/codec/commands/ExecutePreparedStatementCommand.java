package com.xs0.asyncdb.mysql.codec.commands;

import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.mysql.codec.PreparedStatementInfo;
import com.xs0.asyncdb.mysql.codec.statemachine.ExecutePreparedStatementStateMachine;
import com.xs0.asyncdb.mysql.codec.statemachine.MySQLStateMachine;
import com.xs0.asyncdb.mysql.codec.statemachine.TextBasedQueryStateMachine;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExecutePreparedStatementCommand implements MySQLCommand {
    private final PreparedStatementInfo psInfo;
    private final List<Object> values;
    private final CompletableFuture<QueryResult> promise;

    public ExecutePreparedStatementCommand(PreparedStatementInfo psInfo, List<Object> values, CompletableFuture<QueryResult> promise) {
        this.psInfo = psInfo;
        this.values = values;
        this.promise = promise;
    }

    @Override
    public MySQLStateMachine createStateMachine() {
        return new ExecutePreparedStatementStateMachine(psInfo, values, promise);
    }

    @Override
    public CompletableFuture<?> getPromise() {
        return promise;
    }
}
