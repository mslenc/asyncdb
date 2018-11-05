package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ResetConnectionMessage extends ClientMessage {
    public static final ResetConnectionMessage instance = new ResetConnectionMessage();

    private ResetConnectionMessage() {
        // singleton
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer(1);
        contents.writeByte(MyConstants.PACKET_HEADER_RESET_CONNECTION);
        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "RESET_CONNECTION()";
    }
}
