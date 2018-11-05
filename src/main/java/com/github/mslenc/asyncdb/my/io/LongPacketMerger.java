package com.github.mslenc.asyncdb.my.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayList;
import java.util.List;

public class LongPacketMerger extends ByteToMessageDecoder {
    private ArrayList<ByteBuf> previousLargePackets = new ArrayList<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // we have to consume the packet, otherwise our caller thinks the data remains up for grabs

        ByteBuf slice = in.readRetainedSlice(in.readableBytes());

        if (slice.readableBytes() < 0xFFFFFF) {
            if (previousLargePackets.isEmpty()) {
                out.add(slice);
            } else {
                if (slice.readableBytes() > 0)
                    previousLargePackets.add(slice);

                if (previousLargePackets.size() > 1) {
                    CompositeByteBuf composite = ctx.alloc().compositeBuffer(previousLargePackets.size());
                    composite.addComponents(true, previousLargePackets);
                    out.add(composite);
                } else {
                    out.add(previousLargePackets.get(0));
                }

                previousLargePackets.clear();
            }
        } else {
            previousLargePackets.add(slice);
        }
    }
}
