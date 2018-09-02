package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.mysql.message.server.ColumnDefinitionMessage;

import java.util.List;

public class PreparedStatementInfo {
    public final byte[] statementId;
    public final List<ColumnDefinitionMessage> paramDefs;
    public final List<ColumnDefinitionMessage> columnDefs;
    public final boolean[] sentAsLong;
    private boolean longParamsSent;
    private boolean closeSent;

    public PreparedStatementInfo(byte[] statementId, List<ColumnDefinitionMessage> paramDefs, List<ColumnDefinitionMessage> columnDefs) {
        this.statementId = statementId;
        this.paramDefs = paramDefs;
        this.columnDefs = columnDefs;
        this.sentAsLong = new boolean[paramDefs.size()];
    }

    public boolean shouldReset() {
        boolean result = longParamsSent;
        longParamsSent = false;
        return result;
    }

    public void markLongParamsSent() {
        longParamsSent = true;
    }

    public boolean wasClosed() {
        return closeSent;
    }

    public void markAsClosed() {
        this.closeSent = true;
    }
}
