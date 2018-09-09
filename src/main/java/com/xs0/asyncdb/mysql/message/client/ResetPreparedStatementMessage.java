package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.state.MySQLCommand;
import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ResetPreparedStatementMessage extends ClientMessage {
    private final byte[] statementId;

    public ResetPreparedStatementMessage(MySQLCommand command, byte[] statementId) {
        super(command);

        this.statementId = statementId;
    }
    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer(5);
        contents.writeByte(MySQLIO.PACKET_HEADER_STMT_RESET);
        contents.writeBytes(statementId);
        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_RESET(statement_id=" + ByteBufUtils.toHexString(statementId) + ")";
    }
}
