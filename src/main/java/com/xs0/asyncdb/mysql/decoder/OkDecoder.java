package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.OkMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readBinaryLength;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readUntilEOF;

public class OkDecoder {
    public static OkMessage decodeAfterHeader(ByteBuf packet, Charset charset) {
        // https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html

        long affectedRows = readBinaryLength(packet);
        long lastInsertId = readBinaryLength(packet);
        int statusFlags = packet.readUnsignedShortLE();
        int numWarnings = packet.readUnsignedShortLE();
        String message = readUntilEOF(packet, charset);

        return new OkMessage(affectedRows, lastInsertId, statusFlags, numWarnings, message);
    }
}