package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.state.MySQLCommand;
import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;

public class ClosePreparedStatementMessage extends ClientMessage {
    private final byte[] statementId;

    public ClosePreparedStatementMessage(MySQLCommand command, byte[] statementId) {
        super(command);

        this.statementId = statementId;
    }

    @Override
    public ByteBuf getPacketContents() {
        // https://dev.mysql.com/doc/internals/en/com-stmt-close.html

        ByteBuf contents = newMysqlBuffer(5);
        contents.writeByte(MySQLIO.PACKET_HEADER_STMT_CLOSE);
        contents.writeBytes(statementId);

        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_CLOSE(statement_id=" + ByteBufUtils.toHexString(statementId) + ")";
    }
}
