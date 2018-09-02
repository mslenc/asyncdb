package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;
import static com.xs0.asyncdb.mysql.util.MySQLIO.PACKET_HEADER_STMT_SEND_LONG_DATA;

public class SendLongDataMessage extends ClientMessage {
    public final byte[] statementId;
    public final ByteBuf paramValue;
    public final int paramIndex;

    public SendLongDataMessage(byte[] statementId, int paramIndex, ByteBuf paramValue) {
        this.statementId = statementId;
        this.paramValue = paramValue;
        this.paramIndex = paramIndex;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf paramInfo = newMysqlBuffer(1 + 4 + 2);
        paramInfo.writeByte(PACKET_HEADER_STMT_SEND_LONG_DATA);
        paramInfo.writeBytes(statementId);
        paramInfo.writeShortLE(paramIndex);

        return Unpooled.wrappedBuffer(paramInfo, paramValue);
    }
}
