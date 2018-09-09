package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.state.MySQLCommand;
import com.xs0.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QueryMessage extends ClientMessage {
    public final String query;

    public QueryMessage(MySQLCommand command, String query) {
        super(command);
        
        this.query = query;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer(query.length() + 25);
        contents.writeByte(MySQLIO.PACKET_HEDAER_COM_QUERY);
        contents.writeCharSequence(query, UTF_8);
        return contents;
    }

    public String toString(boolean fullDetails) {
        if (fullDetails || query.length() <= 100) {
            return "COM_QUERY(query=\"" + query + "\")";
        } else {
            return "COM_QUERY(query=\"" + query.substring(0, 50) + "[...]" + query.substring(query.length() - 50) + "\")";
        }
    }
}
