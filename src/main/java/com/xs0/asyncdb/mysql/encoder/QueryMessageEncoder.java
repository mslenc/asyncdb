package com.xs0.asyncdb.mysql.encoder;

import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import com.xs0.asyncdb.mysql.message.client.QueryMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newPacketBuffer;

public class QueryMessageEncoder implements MessageEncoder {
    private final Charset charset;

    public QueryMessageEncoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ByteBuf encode(ClientMessage message) {
        QueryMessage m = (QueryMessage) message;

        byte[] encodedQuery = m.query.getBytes(charset);
        ByteBuf buffer = newPacketBuffer(encodedQuery.length + 5);
        buffer.writeByte(ClientMessage.QUERY);
        buffer.writeBytes(encodedQuery);

        return buffer;
    }
}