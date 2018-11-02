package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PreparedStatementExecuteMessage extends ClientMessage {
    private final byte[] statementId;
    private final byte[] nullBytes;
    private final ByteBuf typeBytes;
    private final ByteBuf valueBytes;

    public PreparedStatementExecuteMessage(byte[] statementId, byte[] nullBytes, ByteBuf typeBytes, ByteBuf valueBytes) {
        this.statementId = statementId;
        this.nullBytes = nullBytes;
        this.typeBytes = typeBytes;
        this.valueBytes = valueBytes;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf prefix = Unpooled.buffer(1 + 4 + 1 + 4 + nullBytes.length);
        prefix.writeByte(MyConstants.PACKET_HEADER_STMT_EXECUTE);
        prefix.writeBytes(statementId);
        prefix.writeByte(0); // flags
        prefix.writeIntLE(1); // iteration-count
        prefix.writeBytes(nullBytes);

        return Unpooled.wrappedBuffer(prefix, typeBytes, valueBytes);
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_EXECUTE(statement_id=" + ByteBufUtils.toHexString(statementId) + ", TODO)";
    }
}
