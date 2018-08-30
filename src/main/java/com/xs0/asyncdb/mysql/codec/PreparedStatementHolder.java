package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import com.xs0.asyncdb.mysql.message.server.PreparedStatementPrepareResponse;

import java.util.ArrayList;

public class PreparedStatementHolder {
    final String statement;
    final PreparedStatementPrepareResponse message;

    public PreparedStatementHolder(String statement, PreparedStatementPrepareResponse message) {
        this.statement = statement;
        this.message = message;
    }

    final ArrayList<ColumnDefinitionMessage> columns = new ArrayList<>();
    final ArrayList<ColumnDefinitionMessage> parameters = new ArrayList<>();

    public byte[] statementId() {
        return message.statementId;
    }

    public boolean needsParameters() {
        return message.paramsCount != this.parameters.size();
    }

    public boolean needsColumns() {
        return message.columnsCount != this.columns.size();
    }

    public boolean needsAny() {
        return needsParameters() || needsColumns();
    }

    public void add(ColumnDefinitionMessage column) {
        if (this.needsParameters()) {
            this.parameters.add(column);
        } else
        if (this.needsColumns()) {
            this.columns.add(column);
        } else {
            // throw new IllegalStateException(); // TODO: decide on this
        }
    }

    @Override
    public String toString() {
        return "PreparedStatementHolder(" + statement + ")";
    }
}
