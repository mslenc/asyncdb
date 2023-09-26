package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SSLRequestMessage extends ClientMessage {
    private final int capabilityFlags;

    SSLRequestMessage(int capabilityFlags) {
        this.capabilityFlags = capabilityFlags;
    }

    @Override
    public int getFirstPacketSequenceNumber() {
        // initial handshake must start with sequence number 1, for some unknown reason...
        return 1;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer();

        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse

        contents.writeIntLE(capabilityFlags);
        contents.writeIntLE(MyConstants.MAX_PACKET_LENGTH);
        contents.writeByte(MyConstants.CHARSET_ID_UTF8MB4);
        contents.writeZero(23);

        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "SSLRequest(flags=0b" + Integer.toBinaryString(capabilityFlags) + ")";
    }
}
