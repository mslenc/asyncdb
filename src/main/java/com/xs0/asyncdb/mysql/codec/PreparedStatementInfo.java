package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.mysql.message.server.ColumnDefinitionMessage;

import java.util.List;

public class PreparedStatementInfo {
    final byte[] statementId;
    final List<ColumnDefinitionMessage> paramDefs;
    final List<ColumnDefinitionMessage> columnDefs;

    public PreparedStatementInfo(byte[] statementId, List<ColumnDefinitionMessage> paramDefs, List<ColumnDefinitionMessage> columnDefs) {
        this.statementId = statementId;
        this.paramDefs = paramDefs;
        this.columnDefs = columnDefs;
    }
}
