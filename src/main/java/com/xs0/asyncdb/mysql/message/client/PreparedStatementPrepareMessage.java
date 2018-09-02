package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;
import static com.xs0.asyncdb.mysql.util.MySQLIO.PACKET_HEADER_STMT_PREPARE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PreparedStatementPrepareMessage extends ClientMessage {
    private final String statement;

    public PreparedStatementPrepareMessage(String statement) {
        this.statement = statement;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = newMysqlBuffer(statement.length() + 25);
        contents.writeByte(PACKET_HEADER_STMT_PREPARE);
        contents.writeCharSequence(statement, UTF_8);
        return contents;
    }
}
