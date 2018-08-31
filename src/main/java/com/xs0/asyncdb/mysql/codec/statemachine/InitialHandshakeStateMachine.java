package com.xs0.asyncdb.mysql.codec.statemachine;

import com.xs0.asyncdb.common.Configuration;
import com.xs0.asyncdb.mysql.codec.MySQLConnectionHandler;
import com.xs0.asyncdb.mysql.decoder.ErrorDecoder;
import com.xs0.asyncdb.mysql.decoder.HandshakeV10Decoder;
import com.xs0.asyncdb.mysql.encoder.auth.MySQLNativePasswordAuthentication;
import com.xs0.asyncdb.mysql.encoder.auth.MySQLOldPasswordAuthentication;
import com.xs0.asyncdb.mysql.message.client.AuthenticationSwitchResponse;
import com.xs0.asyncdb.mysql.message.client.HandshakeResponseMessage;
import com.xs0.asyncdb.mysql.message.server.ErrorMessage;
import com.xs0.asyncdb.mysql.message.server.HandshakeMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readCString;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readFixedBytes;
import static com.xs0.asyncdb.mysql.util.MySQLIO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class InitialHandshakeStateMachine implements MySQLStateMachine {
    private static final Logger log = LoggerFactory.getLogger(InitialHandshakeStateMachine.class);

    private static final int STATE_INITIAL = 0;
    private static final int STATE_CLIENT_HANDSHAKE_SENT = 1;

    private int state = STATE_INITIAL;
    private MySQLConnectionHandler conn;
    private int sequenceNumber = 1;

    @Override
    public Result init(MySQLConnectionHandler conn) {
        this.conn = conn;
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        switch (state) {
            case STATE_INITIAL:
                return processPacket_INITIAL(packet);

            case STATE_CLIENT_HANDSHAKE_SENT:
                return processPacket_HANDSHAKE_SENT(packet);

            default:
                throw new IllegalStateException("Invalid state");
        }
    }

    private Result processPacket_INITIAL(ByteBuf packet) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_ERR:
                ErrorMessage error = ErrorDecoder.decodeAfterHeader(packet, StandardCharsets.ISO_8859_1, 0);
                return Result.protocolErrorAbortEverything(error.errorMessage);

            case PACKET_HEADER_HANDSHAKE_V10:
                HandshakeMessage handshake = HandshakeV10Decoder.decodeAfterHeader(packet);
                conn.setServerInfo(handshake);
                HandshakeResponseMessage response = generateHandshakeResponse(handshake.authenticationMethod, handshake.seed);
                conn.sendMessage(response);
                state = STATE_CLIENT_HANDSHAKE_SENT;
                return Result.expectingMorePackets();

            default:
                return Result.unknownHeaderByte(header, "handshake (initial)");
        }
    }

    private Result processPacket_HANDSHAKE_SENT(ByteBuf packet) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_OK:
                log.debug("Authentication successful");
                return Result.stateMachineFinished();

            case PACKET_HEADER_AUTH_SWITCH_REQUEST:
                return respondToAuthSwitchRequest(packet);

            case PACKET_HEADER_ERR:
                ErrorMessage error = ErrorDecoder.decodeAfterHeader(packet, UTF_8, conn.serverInfo().serverCapabilities);
                return Result.protocolErrorAbortEverything(error.errorMessage);

            default:
                return Result.unknownHeaderByte(header, "handshake (later)");
        }
    }

    private Result respondToAuthSwitchRequest(ByteBuf packet) {
        String pluginName = readCString(packet, UTF_8);
        if (pluginName == null)
            return Result.protocolErrorAbortEverything("Couldn't read pluginName during auth");
        byte[] pluginData = readFixedBytes(packet, packet.readableBytes());

        byte[] auth = generateAuth(pluginName, pluginData, conn.configuration.password, false);
        if (auth == null) {
            return Result.protocolErrorAbortEverything("Unsupported authentication mechanism ("+pluginName+") requested by server");
        } else {
            conn.sendMessage(new AuthenticationSwitchResponse(auth, ++sequenceNumber));
            return Result.expectingMorePackets();
        }
    }

    private HandshakeResponseMessage generateHandshakeResponse(String authenticationMethod, byte[] authSeed) {
        Configuration config = conn.configuration;

        HandshakeResponseMessage response = new HandshakeResponseMessage(config.username);

        if (authenticationMethod != null)
            response.setAuthMethod(authenticationMethod, generateAuth(authenticationMethod, authSeed, config.password, true));

        if (config.database != null)
            response.setDatabase(config.database);

        return response;
    }

    private byte[] generateAuth(String authenticationMethod, byte[] authSeed, String password, boolean defaultUnrecognizedToNative) {
        switch (authenticationMethod) {
            case AUTH_OLD:
                return MySQLOldPasswordAuthentication.generateAuthentication(UTF_8, password, authSeed);

            default:
                if (!defaultUnrecognizedToNative)
                    return null;
                // else fall through

            case AUTH_NATIVE:
                return MySQLNativePasswordAuthentication.generateAuthentication(UTF_8, password, authSeed);
        }
    }
}
