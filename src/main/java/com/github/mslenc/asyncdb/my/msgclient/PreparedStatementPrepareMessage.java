package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PreparedStatementPrepareMessage extends ClientMessage {
    private final String statement;

    public PreparedStatementPrepareMessage(String statement) {
        this.statement = statement;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer(statement.length() + 25);
        contents.writeByte(MyConstants.PACKET_HEADER_STMT_PREPARE);
        contents.writeCharSequence(statement, UTF_8);
        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        if (fullDetails || statement.length() <= 100) {
            return "STMT_PREPARE(query=\"" + statement + "\")";
        } else {
            return "STMT_PREPARE(query=\"" + statement.substring(0, 50) + "[...]" + statement.substring(statement.length() - 50) + "\")";
        }
    }
}
