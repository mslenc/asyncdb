package com.xs0.asyncdb.mysql.message.client;

public class PreparedStatementPrepareMessage implements ClientMessage {
    public final String statement;

    public PreparedStatementPrepareMessage(String statement) {
        this.statement = statement;
    }

    @Override
    public int kind() {
        return PREPARED_STATEMENT_PREPARE;
    }
}
