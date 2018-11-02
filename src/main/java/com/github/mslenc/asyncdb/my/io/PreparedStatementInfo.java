package com.github.mslenc.asyncdb.my.io;

import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.impl.DbColumnsImpl;
import com.github.mslenc.asyncdb.my.msgserver.ColumnDefinitionMessage;

import java.util.List;

public class PreparedStatementInfo {
    public final byte[] statementId;
    public final List<ColumnDefinitionMessage> paramDefs;
    public final List<ColumnDefinitionMessage> columnDefs;
    public final boolean[] sentAsLong;
    private final DbColumnsImpl parameters;
    private final DbColumnsImpl columns;
    private boolean longParamsSent;
    private boolean closeSent;

    public PreparedStatementInfo(byte[] statementId, List<ColumnDefinitionMessage> paramDefs, List<ColumnDefinitionMessage> columnDefs) {
        this.statementId = statementId;
        this.paramDefs = paramDefs;
        this.columnDefs = columnDefs;
        this.parameters = new DbColumnsImpl(paramDefs);
        this.columns = new DbColumnsImpl(columnDefs);
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

    public DbColumns getColumns() {
        return columns;
    }

    public DbColumns getParameters() {
        return parameters;
    }
}
