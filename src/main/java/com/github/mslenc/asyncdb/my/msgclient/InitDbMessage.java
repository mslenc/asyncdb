package com.github.mslenc.asyncdb.my.msgclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.github.mslenc.asyncdb.my.MyConstants.PACKET_HEADER_INIT_DB;
import static java.nio.charset.StandardCharsets.UTF_8;

public class InitDbMessage extends ClientMessage {
    private final String database;

    public InitDbMessage(String database) {
        this.database = database;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf res = Unpooled.buffer(database.length() + 1);
        res.writeByte(PACKET_HEADER_INIT_DB);
        res.writeCharSequence(database, UTF_8);
        return res;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "INIT_DB(database=" + database + ")";
    }
}
