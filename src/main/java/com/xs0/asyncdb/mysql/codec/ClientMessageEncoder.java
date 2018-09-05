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

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newMysqlBuffer;

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
        ByteBuf header = newMysqlBuffer(4);
        ByteBuf contents = message.getPacketContents();

        header.writeMediumLE(contents.readableBytes());
        header.writeByte(message.getCommand().nextPacketSequenceNumber());

        ByteBuf packet = Unpooled.wrappedBuffer(header, contents);
        out.add(packet);

        if (log.isTraceEnabled()) {
            log.trace("Sending message {} - \n{}", message.getClass().getName(), BufferDumper.dumpAsHex(packet));
        }
    }
}
