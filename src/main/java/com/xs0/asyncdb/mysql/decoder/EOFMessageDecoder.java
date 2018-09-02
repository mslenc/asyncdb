package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.EOFMessage;
import io.netty.buffer.ByteBuf;

public class EOFMessageDecoder {
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
