package com.github.mslenc.asyncdb.mysql.message.client;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClosePreparedStatementMessage extends ClientMessage {
    private final byte[] statementId;

    public ClosePreparedStatementMessage(byte[] statementId) {
        this.statementId = statementId;
    }

    @Override
    public ByteBuf getPacketContents() {
        // https://dev.mysql.com/doc/internals/en/com-stmt-close.html

        ByteBuf contents = Unpooled.buffer(5);
        contents.writeByte(MySQLIO.PACKET_HEADER_STMT_CLOSE);
        contents.writeBytes(statementId);

        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_CLOSE(statement_id=" + ByteBufUtils.toHexString(statementId) + ")";
    }
}
