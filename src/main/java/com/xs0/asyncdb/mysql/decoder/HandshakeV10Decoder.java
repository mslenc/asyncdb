package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.encoder.auth.AuthenticationMethod;
import com.xs0.asyncdb.mysql.message.server.HandshakeMessage;
import com.xs0.asyncdb.mysql.message.server.ServerMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readCString;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readUntilEOF;
import static com.xs0.asyncdb.mysql.util.MySQLIO.CLIENT_PLUGIN_AUTH;
import static com.xs0.asyncdb.mysql.util.MySQLIO.CLIENT_SECURE_CONNECTION;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class HandshakeV10Decoder implements MessageDecoder {
    private static final HandshakeV10Decoder instance = new HandshakeV10Decoder();

    public static HandshakeV10Decoder instance() {
        return instance;
    }

    private static final Logger log = LoggerFactory.getLogger(HandshakeV10Decoder.class);
    private static final int seedSize = 8;
    private static final int seedComplementSize = 12;
    private static final int padding = 10;

    @Override
    public ServerMessage decode(ByteBuf buffer) {
        String serverVersion = readCString(buffer, US_ASCII);
        long connectionId = buffer.readUnsignedInt();

        byte[] seed = new byte[seedSize + seedComplementSize];
        buffer.readBytes(seed, 0, seedSize);

        buffer.readByte(); // filler

        int serverCapabilityFlags = buffer.readUnsignedShort();

        /* New protocol with 16 bytes to describe server characteristics */

        // read character set (1 byte)
        int characterSet = buffer.readUnsignedByte();

        // read status flags (2 bytes)
        int statusFlags = buffer.readUnsignedShort();

        // read capability flags (upper 2 bytes)
        serverCapabilityFlags |= buffer.readUnsignedShort() << 16;

        int authPluginDataLength = 0;
        String authenticationMethod = AuthenticationMethod.NATIVE;

        if ((serverCapabilityFlags & CLIENT_PLUGIN_AUTH) != 0) {
            // read length of auth-plugin-data (1 byte)
            authPluginDataLength = buffer.readUnsignedByte();
        } else {
            // read filler ([00])
            buffer.readByte();
        }

        // next 10 bytes are reserved (all [00])
        buffer.skipBytes(padding);

        log.debug("Auth plugin data length was {}", authPluginDataLength);

        if ((serverCapabilityFlags & CLIENT_SECURE_CONNECTION) != 0) {
            int complement;
            if (authPluginDataLength > 0) {
                complement = authPluginDataLength - 1 - seedSize;
            } else {
                complement = seedComplementSize;
            }

            buffer.readBytes(seed, seedSize, complement);
            buffer.readByte();
        }

        if ((serverCapabilityFlags & CLIENT_PLUGIN_AUTH) != 0) {
            authenticationMethod = readUntilEOF(buffer, US_ASCII);
        }

        HandshakeMessage message = new HandshakeMessage(
            serverVersion,
            connectionId,
            seed,
            serverCapabilityFlags,
            characterSet,
            statusFlags,
            authenticationMethod
        );

        log.debug("handshake message was {}", message);

        return message;
    }
}
