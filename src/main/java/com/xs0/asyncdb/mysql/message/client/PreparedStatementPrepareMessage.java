package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.util.MySQLIO.PACKET_HEADER_STMT_PREPARE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PreparedStatementPrepareMessage implements ClientMessage {
    public final String statement;

    public PreparedStatementPrepareMessage(String statement) {
        this.statement = statement;
    }

    @Override
    public void encodeInto(ByteBuf packet) {
        packet.writeByte(PACKET_HEADER_STMT_PREPARE);
        packet.writeCharSequence(statement, UTF_8);
    }

    @Override
    public int packetSequenceNumber() {
        return 0;
    }
}
