package com.xs0.asyncdb.mysql.message.server;

public class PreparedStatementPrepareResponse implements ServerMessage {
    public final byte[] statementId;
    public final short warningCount;
    public final int paramsCount;
    public final int columnsCount;

    public PreparedStatementPrepareResponse(byte[] statementId, short warningCount, int paramsCount, int columnsCount) {
        this.statementId = statementId;
        this.warningCount = warningCount;
        this.paramsCount = paramsCount;
        this.columnsCount = columnsCount;
    }

    @Override
    public int kind() {
        return PreparedStatementPrepareResponse;
    }
}
