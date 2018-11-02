package com.github.mslenc.asyncdb.my.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayList;
import java.util.List;

public class LongPacketMerger extends ByteToMessageDecoder {
    private ArrayList<ByteBuf> previousLargePackets = new ArrayList<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // we have to consume the packet, otherwise our caller thinks the data remains up for grabs
        // TODO: slice and/or retain instead, to avoid the copying
        in = in.readBytes(in.readableBytes());

        if (in.readableBytes() >= 0xFFFFFF) {
            previousLargePackets.add(in);
        } else {
            if (previousLargePackets.isEmpty()) {
                out.add(in);
            } else {
                if (in.readableBytes() > 0)
                    previousLargePackets.add(in);

                ByteBuf[] parts = new ByteBuf[previousLargePackets.size()];
                previousLargePackets.toArray(parts);
                previousLargePackets.clear();

                out.add(Unpooled.wrappedBuffer(parts));
            }
        }
    }
}
