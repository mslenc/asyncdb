package com.xs0.asyncdb.mysql.encoder;

import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import com.xs0.asyncdb.mysql.message.client.PreparedStatementPrepareMessage;
import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newPacketBuffer;

public class PreparedStatementPrepareEncoder implements MessageEncoder {
    private final Charset charset;

    public PreparedStatementPrepareEncoder(Charset charset  ) {
        this.charset = charset;
    }

    @Override
    public ByteBuf encode(ClientMessage message) {
        PreparedStatementPrepareMessage m = (PreparedStatementPrepareMessage) message;

        byte[] statement = m.statement.getBytes(charset);
        ByteBuf buffer = newPacketBuffer( 5 + statement.length);
        buffer.writeByte(m.kind());
        buffer.writeBytes(statement);

        return buffer;
    }
}