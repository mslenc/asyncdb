package com.github.mslenc.asyncdb.mysql.decoder;

import com.github.mslenc.asyncdb.mysql.message.server.PreparedStatementPrepareResponse;
import io.netty.buffer.ByteBuf;

public class PreparedStatementPrepareResponseDecoder {
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
