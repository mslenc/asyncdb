package com.github.mslenc.asyncdb.mysql.message.client;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SendLongDataMessage extends ClientMessage {
    public final byte[] statementId;
    public final ByteBuf paramValue;
    public final int paramIndex;

    public SendLongDataMessage(MySQLCommand command, byte[] statementId, int paramIndex, ByteBuf paramValue) {
        super(command);

        this.statementId = statementId;
        this.paramValue = paramValue;
        this.paramIndex = paramIndex;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf paramInfo = Unpooled.buffer(1 + 4 + 2);
        paramInfo.writeByte(MySQLIO.PACKET_HEADER_STMT_SEND_LONG_DATA);
        paramInfo.writeBytes(statementId);
        paramInfo.writeShortLE(paramIndex);

        return Unpooled.wrappedBuffer(paramInfo, paramValue);
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_SEND_LONG_DATA(statementId=" + ByteBufUtils.toHexString(statementId) + ", value=" + paramValue.readableBytes() + " bytes)";
    }
}
