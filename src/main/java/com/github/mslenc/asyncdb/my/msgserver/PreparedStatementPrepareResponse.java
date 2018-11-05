package com.github.mslenc.asyncdb.my.msgserver;

import io.netty.buffer.ByteBuf;

public class PreparedStatementPrepareResponse {
    public final int statementId;
    public final short warningCount;
    public final int paramsCount;
    public final int columnsCount;

    public PreparedStatementPrepareResponse(int statementId, short warningCount, int paramsCount, int columnsCount) {
        this.statementId = statementId;
        this.warningCount = warningCount;
        this.paramsCount = paramsCount;
        this.columnsCount = columnsCount;
    }

    public static PreparedStatementPrepareResponse decodeAfterHeader(ByteBuf buffer) {
        int statementId = buffer.readIntLE();
        int columnsCount = buffer.readUnsignedShortLE();
        int paramsCount = buffer.readUnsignedShortLE();

        // filler
        buffer.readByte();

        short warningCount = buffer.readShortLE();

        return new PreparedStatementPrepareResponse(statementId, warningCount, paramsCount, columnsCount);
    }
}
