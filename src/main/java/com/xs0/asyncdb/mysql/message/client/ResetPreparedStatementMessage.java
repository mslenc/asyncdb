package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;

public class ResetPreparedStatementMessage extends ClientMessage {
    private final byte[] statementId;

    public ResetPreparedStatementMessage(byte[] statementId) {
        this.statementId = statementId;
    }
    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = newMysqlBuffer(5);
        contents.writeByte(MySQLIO.PACKET_HEADER_STMT_RESET);
        contents.writeBytes(statementId);
        return contents;
    }
}
