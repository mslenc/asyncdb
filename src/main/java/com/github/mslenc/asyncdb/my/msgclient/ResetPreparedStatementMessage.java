package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ResetPreparedStatementMessage extends ClientMessage {
    private final int statementId;

    public ResetPreparedStatementMessage(int statementId) {
        this.statementId = statementId;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer(5);
        contents.writeByte(MyConstants.PACKET_HEADER_STMT_RESET);
        contents.writeIntLE(statementId);
        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_RESET(statement_id=" + statementId + ")";
    }
}
