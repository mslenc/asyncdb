package com.github.mslenc.asyncdb.my.msgserver;

import com.github.mslenc.asyncdb.ex.ProtocolException;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static com.github.mslenc.asyncdb.my.MyConstants.CLIENT_PLUGIN_AUTH;
import static com.github.mslenc.asyncdb.my.MyConstants.CLIENT_SECURE_CONNECTION;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readCString;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readFixedBytes;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readUntilEOFOrZero;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class HandshakeMessage {
    private static final Logger log = LoggerFactory.getLogger(HandshakeMessage.class);

    public final String serverVersion;
    public final long connectionId;
    public final byte[] seed;
    public final int serverCapabilities;
    public final int characterSet;
    public final int statusFlags;
    public final String authenticationMethod;

    public HandshakeMessage(String serverVersion, long connectionId, byte[] seed, int serverCapabilities, int characterSet, int statusFlags, String authenticationMethod) {
        this.serverVersion = serverVersion;
        this.connectionId = connectionId;
        this.seed = seed;
        this.serverCapabilities = serverCapabilities;
        this.characterSet = characterSet;
        this.statusFlags = statusFlags;
        this.authenticationMethod = authenticationMethod;
    }

    @Override
    public String toString() {
        return "HandshakeMessage{" +
                "serverVersion='" + serverVersion + '\'' +
                ", connectionId=" + connectionId +
                ", serverCapabilities=0b" + Integer.toBinaryString(serverCapabilities) +
                ", characterSet=" + characterSet +
                ", statusFlags=0b" + Integer.toBinaryString(statusFlags) +
                ", authenticationMethod='" + authenticationMethod + '\'' +
                '}';
    }

    public static HandshakeMessage decodeAfterHeader(ByteBuf packet) {
        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake

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
                authPluginName = readUntilEOFOrZero(packet, StandardCharsets.ISO_8859_1);
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
