package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.PreparedStatementPrepareResponse;
import com.xs0.asyncdb.mysql.message.server.ServerMessage;
import io.netty.buffer.ByteBuf;

public class PreparedStatementPrepareResponseDecoder implements MessageDecoder {
    private static final PreparedStatementPrepareResponseDecoder instance = new PreparedStatementPrepareResponseDecoder();

    public static PreparedStatementPrepareResponseDecoder instance() {
        return instance;
    }

    @Override
    public ServerMessage decode(ByteBuf buffer) {
        byte[] statementId = new byte[4];
        buffer.readBytes(statementId);

        int columnsCount = buffer.readUnsignedShort();
        int paramsCount = buffer.readUnsignedShort();

        // filler
        buffer.readByte();

        short warningCount = buffer.readShort();

        return new PreparedStatementPrepareResponse(statementId, warningCount, paramsCount, columnsCount);
    }
}
