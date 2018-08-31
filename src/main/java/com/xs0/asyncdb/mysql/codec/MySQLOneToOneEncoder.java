package com.xs0.asyncdb.mysql.codec;

import java.util.List;

import com.xs0.asyncdb.common.util.BufferDumper;
import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.writePacketLength;

public class MySQLOneToOneEncoder extends MessageToMessageEncoder<ClientMessage> {
    private static final Logger log = LoggerFactory.getLogger(MySQLOneToOneEncoder.class);

    private static final MySQLOneToOneEncoder instance = new MySQLOneToOneEncoder();

    public static MySQLOneToOneEncoder instance() {
        return instance;
    }

    private MySQLOneToOneEncoder() {
        super(ClientMessage.class);
    }

    public void encode(ChannelHandlerContext ctx, ClientMessage message, List<Object> out) {
        ByteBuf packet = ByteBufUtils.newPacketBuffer();
        message.encodeInto(packet);
        writePacketLength(packet, message.packetSequenceNumber());
        out.add(packet);

        if (log.isTraceEnabled()) {
            log.trace("Sending message {} - \n{}", message.getClass().getName(), BufferDumper.dumpAsHex(packet));
        }
    }
}
