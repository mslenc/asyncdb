package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import com.xs0.asyncdb.mysql.message.client.SendLongDataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.write3BytesInt;

class SendLongDataEncoder extends MessageToMessageEncoder<SendLongDataMessage> {
    private static final Logger log = LoggerFactory.getLogger(SendLongDataEncoder.class);
    public static final long LONG_THRESHOLD = 1023;

    private static final SendLongDataEncoder instance = new SendLongDataEncoder();

    public static SendLongDataEncoder instance() {
        return instance;
    }

    SendLongDataEncoder() {
        super(SendLongDataMessage.class);
    }

    public void encode(ChannelHandlerContext ctx, SendLongDataMessage message, List<Object> out) {
        if (log.isTraceEnabled()) {
            log.trace("Writing message {}", message);
        }

        int sequence = 0;

        ByteBuf headerBuffer = newMysqlBuffer(3 + 1 + 1 + 4 + 2);

        write3BytesInt(headerBuffer, 1 + 4 + 2 + message.value.readableBytes());
        headerBuffer.writeByte(sequence);

        headerBuffer.writeByte(ClientMessage.PREPARED_STATEMENT_SEND_LONG_DATA);
        headerBuffer.writeBytes(message.statementId);
        headerBuffer.writeShort(message.paramId);

        ByteBuf result = Unpooled.wrappedBuffer(headerBuffer, message.value);

        out.add(result);
    }
}
