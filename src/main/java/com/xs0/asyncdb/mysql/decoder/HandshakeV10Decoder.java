package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.common.exceptions.ProtocolException;
import com.xs0.asyncdb.common.util.BufferDumper;
import com.xs0.asyncdb.mysql.message.server.HandshakeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kotlin.text.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.Buffer;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readCString;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readFixedBytes;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readUntilEOFOrZero;
import static com.xs0.asyncdb.mysql.util.MySQLIO.CLIENT_PLUGIN_AUTH;
import static com.xs0.asyncdb.mysql.util.MySQLIO.CLIENT_SECURE_CONNECTION;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class HandshakeV10Decoder {
    // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake

    private static final Logger log = LoggerFactory.getLogger(HandshakeV10Decoder.class);

    public static HandshakeMessage decodeAfterHeader(ByteBuf packet) {
        String serverVersion = readCString(packet, US_ASCII);
        if (serverVersion == null)
            throw new ProtocolException("Failed to read server version");

        long connectionId = packet.readUnsignedIntLE();

        byte[] authPluginData = readFixedBytes(packet, 8);
        if (authPluginData == null)
            throw new ProtocolException("Incomplete auth-plugin-data-part-1");

        packet.skipBytes(1); // filler

        int capabilityFlags = packet.readUnsignedShortLE();

        String authPluginName = null;

        int charsetId = -1;
        int statusFlags = 0;

        if (packet.readableBytes() > 0) {
            charsetId = packet.readUnsignedByte();
            statusFlags = packet.readUnsignedShortLE();
            capabilityFlags |= packet.readUnsignedShortLE() << 16;

            int lengthOfAuthPluginData = 0;
            if ((capabilityFlags & CLIENT_PLUGIN_AUTH) != 0) {
                lengthOfAuthPluginData = packet.readUnsignedByte();
                System.err.println("Length of auth Plugin DATA = " + lengthOfAuthPluginData);
            } else {
                packet.skipBytes(1);
            }

            packet.skipBytes(10); // reserved

            if ((capabilityFlags & CLIENT_SECURE_CONNECTION) != 0) {
                int extraBytes = Math.max(13, lengthOfAuthPluginData - 8);
                byte[] fullAuthPluginData = new byte[lengthOfAuthPluginData];
                System.arraycopy(authPluginData, 0, fullAuthPluginData, 0, 8);
                packet.readBytes(fullAuthPluginData, 8, extraBytes);
                authPluginData = fullAuthPluginData;
            }

            if ((capabilityFlags & CLIENT_PLUGIN_AUTH) != 0) {
                authPluginName = readUntilEOFOrZero(packet, Charsets.ISO_8859_1);
            }
        }

        HandshakeMessage message = new HandshakeMessage(
            serverVersion,
            connectionId,
            authPluginData,
            capabilityFlags,
            charsetId,
            statusFlags,
            authPluginName
        );

        log.debug("handshake message was {}", message);

        return message;
    }
}
