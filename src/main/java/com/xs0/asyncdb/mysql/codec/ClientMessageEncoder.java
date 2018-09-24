package com.xs0.asyncdb.mysql.codec;

import java.util.List;

import com.xs0.asyncdb.common.util.BufferDumper;
import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ClientMessageEncoder extends MessageToMessageEncoder<ClientMessage> {
    private static final Logger log = LoggerFactory.getLogger(ClientMessageEncoder.class);

    private static final ClientMessageEncoder instance = new ClientMessageEncoder();

    public static ClientMessageEncoder instance() {
        return instance;
    }

    private ClientMessageEncoder() {
        super(ClientMessage.class);
    }

    public void encode(ChannelHandlerContext ctx, ClientMessage message, List<Object> out) {
        ByteBuf header = Unpooled.buffer(4);
        ByteBuf contents = message.getPacketContents();

        int readableBytes = contents.readableBytes();
        int sequenceNumber = message.getCommand().firstPacketSequenceNumber();

        log.debug("Converted a message of {} with sequence number {}", readableBytes, sequenceNumber);

        header.writeMediumLE(readableBytes);
        header.writeByte(sequenceNumber);

        out.add(header);
        out.add(contents);

        if (log.isTraceEnabled()) {
            log.trace("Sending header {} - \n{}", message.getClass().getName(), BufferDumper.dumpAsHex(header));
            log.trace("Sending message {} - \n{}", message.getClass().getName(), BufferDumper.dumpAsHex(contents));
        }
    }
}
