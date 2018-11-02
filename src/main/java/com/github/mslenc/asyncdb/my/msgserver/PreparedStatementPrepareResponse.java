package com.github.mslenc.asyncdb.my.msgserver;

import io.netty.buffer.ByteBuf;

public class PreparedStatementPrepareResponse {
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

    public static PreparedStatementPrepareResponse decodeAfterHeader(ByteBuf buffer) {
        byte[] statementId = new byte[4];
        buffer.readBytes(statementId);

        int columnsCount = buffer.readUnsignedShortLE();
        int paramsCount = buffer.readUnsignedShortLE();

        // filler
        buffer.readByte();

        short warningCount = buffer.readShortLE();

        return new PreparedStatementPrepareResponse(statementId, warningCount, paramsCount, columnsCount);
    }
}
