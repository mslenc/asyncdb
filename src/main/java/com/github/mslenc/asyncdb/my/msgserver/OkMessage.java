package com.github.mslenc.asyncdb.my.msgserver;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.readBinaryLength;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readUntilEOF;

public class OkMessage {
    public final long affectedRows;
    public final long lastInsertId;
    public final int statusFlags;
    public final int warnings;
    public final String message;

    public OkMessage(long affectedRows, long lastInsertId, int statusFlags, int warnings, String message) {
        this.affectedRows = affectedRows;
        this.lastInsertId = lastInsertId;
        this.statusFlags = statusFlags;
        this.warnings = warnings;
        this.message = message;
    }

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
