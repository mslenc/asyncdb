package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;
import static com.xs0.asyncdb.mysql.util.MySQLIO.PACKET_HEADER_STMT_EXECUTE;

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
        ByteBuf prefix = newMysqlBuffer(1 + 4 + 1 + 4);
        prefix.writeByte(PACKET_HEADER_STMT_EXECUTE);
        prefix.writeBytes(statementId);
        prefix.writeByte(0); // flags
        prefix.writeIntLE(1); // iteration-count

        ByteBuf nullBytes = Unpooled.wrappedBuffer(this.nullBytes);

        return Unpooled.wrappedBuffer(prefix, nullBytes, typeBytes, valueBytes);
    }
}
