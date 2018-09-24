package com.github.mslenc.asyncdb.mysql.message.client;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PreparedStatementExecuteMessage extends ClientMessage {
    private final byte[] statementId;
    private final byte[] nullBytes;
    private final ByteBuf typeBytes;
    private final ByteBuf valueBytes;

    public PreparedStatementExecuteMessage(MySQLCommand command, byte[] statementId, byte[] nullBytes, ByteBuf typeBytes, ByteBuf valueBytes) {
        super(command);

        this.statementId = statementId;
        this.nullBytes = nullBytes;
        this.typeBytes = typeBytes;
        this.valueBytes = valueBytes;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf prefix = Unpooled.buffer(1 + 4 + 1 + 4);
        prefix.writeByte(MySQLIO.PACKET_HEADER_STMT_EXECUTE);
        prefix.writeBytes(statementId);
        prefix.writeByte(0); // flags
        prefix.writeIntLE(1); // iteration-count

        ByteBuf nullBytes = Unpooled.wrappedBuffer(this.nullBytes);

        return Unpooled.wrappedBuffer(prefix, nullBytes, typeBytes, valueBytes);
    }

    @Override
    public String toString(boolean fullDetails) {
        return "STMT_EXECUTE(statement_id=" + ByteBufUtils.toHexString(statementId) + ", TODO)";
    }
}
