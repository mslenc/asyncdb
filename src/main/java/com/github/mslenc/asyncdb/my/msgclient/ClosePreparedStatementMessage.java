package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClosePreparedStatementMessage extends ClientMessage {
    private final int statementId;

    public ClosePreparedStatementMessage(int statementId) {
        this.statementId = statementId;
    }

    @Override
    public ByteBuf getPacketContents() {
        // https://dev.mysql.com/doc/internals/en/com-stmt-close.html

        ByteBuf contents = Unpooled.buffer(5);
        contents.writeByte(MyConstants.PACKET_HEADER_STMT_CLOSE);
        contents.writeIntLE(statementId);

        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_CLOSE(statement_id=" + statementId + ")";
    }
}
