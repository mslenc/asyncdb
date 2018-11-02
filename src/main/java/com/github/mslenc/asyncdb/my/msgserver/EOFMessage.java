package com.github.mslenc.asyncdb.my.msgserver;

import io.netty.buffer.ByteBuf;

public class EOFMessage {
    public final int warningCount;
    public final int flags;

    public EOFMessage(int warningCount, int flags) {
        this.warningCount = warningCount;
        this.flags = flags;
    }

    public static EOFMessage decode(ByteBuf buffer) {
        buffer.readByte();
        return decodeAfterHeader(buffer);
    }

    public static EOFMessage decodeAfterHeader(ByteBuf buffer) {
        // https://dev.mysql.com/doc/internals/en/packet-EOF_Packet.html

        int numWarnings = buffer.readUnsignedShortLE();
        int statusFlags = buffer.readUnsignedShortLE();

        return new EOFMessage(numWarnings, statusFlags);
    }
}
