package com.github.mslenc.asyncdb.mysql.message.client;

import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class QueryMessage extends ClientMessage {
    public final ByteBuf queryUtf8;

    public QueryMessage(ByteBuf queryUtf8) {
        this.queryUtf8 = queryUtf8;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf header = Unpooled.buffer(1);
        header.writeByte(MySQLIO.PACKET_HEDAER_COM_QUERY);

        return Unpooled.wrappedBuffer(header, queryUtf8);
    }

    public String toString(boolean fullDetails) {
        CharSequence query = queryUtf8.getCharSequence(queryUtf8.readerIndex(), queryUtf8.readableBytes(), StandardCharsets.UTF_8);

        if (fullDetails || query.length() <= 100) {
            return "COM_QUERY(query=\"" + query + "\")";
        } else {
            return "COM_QUERY(query=\"" + query.subSequence(0, 50) + "[...]" + query.subSequence(query.length() - 50, query.length()) + "\")";
        }
    }
}
