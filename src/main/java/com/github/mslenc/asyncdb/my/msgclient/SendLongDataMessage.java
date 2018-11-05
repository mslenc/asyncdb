package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SendLongDataMessage extends ClientMessage {
    public final int statementId;
    public final ByteBuf paramValue;
    public final int paramIndex;

    public SendLongDataMessage(int statementId, int paramIndex, ByteBuf paramValue) {
        this.statementId = statementId;
        this.paramValue = paramValue;
        this.paramIndex = paramIndex;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf paramInfo = Unpooled.buffer(1 + 4 + 2);
        paramInfo.writeByte(MyConstants.PACKET_HEADER_STMT_SEND_LONG_DATA);
        paramInfo.writeIntLE(statementId);
        paramInfo.writeShortLE(paramIndex);

        return Unpooled.wrappedBuffer(paramInfo, paramValue);
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_SEND_LONG_DATA(statementId=" + statementId + ", value=" + paramValue.readableBytes() + " bytes)";
    }
}
