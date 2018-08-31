package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QueryMessage implements ClientMessage {
    public final String query;

    public QueryMessage(String query) {
        this.query = query;
    }

    @Override
    public void encodeInto(ByteBuf packet) {
        packet.writeByte(MySQLIO.PACKET_HEDAER_COM_QUERY);
        packet.writeCharSequence(query, UTF_8);
    }

    @Override
    public int packetSequenceNumber() {
        return 0;
    }
}
