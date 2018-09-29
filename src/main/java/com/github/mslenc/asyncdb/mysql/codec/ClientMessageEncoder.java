package com.github.mslenc.asyncdb.mysql.codec;

import java.util.List;

import com.github.mslenc.asyncdb.mysql.message.client.ClientMessage;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
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
        ByteBuf contents = message.getPacketContents();
        int sequenceNum = message.getFirstPacketSequenceNumber();

        while (contents.readableBytes() >= MySQLIO.MAX_PACKET_LENGTH) {
            ByteBuf header = Unpooled.buffer(4);
            header.writeMediumLE(MySQLIO.MAX_PACKET_LENGTH);
            header.writeByte(sequenceNum++);

            out.add(header);
            out.add(contents.readRetainedSlice(MySQLIO.MAX_PACKET_LENGTH));
        }

        ByteBuf finalHeader = Unpooled.buffer(4);
        finalHeader.writeMediumLE(contents.readableBytes());
        finalHeader.writeByte(sequenceNum);

        out.add(finalHeader);
        out.add(contents);
    }
}