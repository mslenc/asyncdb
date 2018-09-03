package com.xs0.asyncdb.mysql.state.commands;

import com.xs0.asyncdb.common.Configuration;
import com.xs0.asyncdb.mysql.auth.MySQLNativePasswordAuthentication;
import com.xs0.asyncdb.mysql.auth.MySQLOldPasswordAuthentication;
import com.xs0.asyncdb.mysql.codec.MySQLConnectionHandler;
import com.xs0.asyncdb.mysql.decoder.ErrorDecoder;
import com.xs0.asyncdb.mysql.decoder.HandshakeV10Decoder;
import com.xs0.asyncdb.mysql.message.client.AuthenticationSwitchResponse;
import com.xs0.asyncdb.mysql.message.client.HandshakeResponseMessage;
import com.xs0.asyncdb.mysql.message.server.ErrorMessage;
import com.xs0.asyncdb.mysql.message.server.HandshakeMessage;
import com.xs0.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readCString;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readFixedBytes;
import static com.xs0.asyncdb.mysql.util.MySQLIO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class InitialHandshakeCommand extends MySQLCommand {
    private static final Logger log = LoggerFactory.getLogger(InitialHandshakeCommand.class);

    private static final int STATE_INITIAL = 0;
    private static final int STATE_CLIENT_HANDSHAKE_SENT = 1;

    private final MySQLConnectionHandler conn;
    private final CompletableFuture<MySQLConnectionHandler> promise;
    private final Configuration conf;

    private int state = STATE_INITIAL;

    public InitialHandshakeCommand(MySQLConnectionHandler conn, Configuration conf, CompletableFuture<MySQLConnectionHandler> promise) {
        this.conn = conn;
        this.promise = promise;
        this.conf = conf;

        // initial handshake must start with sequence number 1...
        nextPacketSequenceNumber();
    }

    public CompletableFuture<MySQLConnectionHandler> getPromise() {
        return promise;
    }

    @Override
    public Result start(Support support) {
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet, Support support) {
        switch (state) {
            case STATE_INITIAL:
                return processPacket_INITIAL(packet, support);

            case STATE_CLIENT_HANDSHAKE_SENT:
                return processPacket_HANDSHAKE_SENT(packet, support);

            default:
                throw new IllegalStateException("Invalid state");
        }
    }

    private Result processPacket_INITIAL(ByteBuf packet, Support support) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_ERR:
                ErrorMessage error = ErrorDecoder.decodeAfterHeader(packet, StandardCharsets.ISO_8859_1, 0);
                return Result.protocolErrorAbortEverything(error.errorMessage);

            case PACKET_HEADER_HANDSHAKE_V10:
                HandshakeMessage handshake = HandshakeV10Decoder.decodeAfterHeader(packet);
                conn.setServerInfo(handshake);

                HandshakeResponseMessage response = generateHandshakeResponse(handshake.authenticationMethod, handshake.seed, conf);
                support.sendMessage(response);
                state = STATE_CLIENT_HANDSHAKE_SENT;
                return Result.expectingMorePackets();

            default:
                return Result.unknownHeaderByte(header, "handshake (initial)");
        }
    }

    private Result processPacket_HANDSHAKE_SENT(ByteBuf packet, Support support) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_OK:
                log.debug("Authentication successful");
                promise.complete(conn);
                return Result.stateMachineFinished();

            case PACKET_HEADER_AUTH_SWITCH_REQUEST:
                return respondToAuthSwitchRequest(packet, support, conf);

            case PACKET_HEADER_ERR:
                ErrorMessage error = ErrorDecoder.decodeAfterHeader(packet, UTF_8, conn.serverInfo().serverCapabilities);
                return Result.protocolErrorAbortEverything(error.errorMessage);

            default:
                return Result.unknownHeaderByte(header, "handshake (later)");
        }
    }

    private Result respondToAuthSwitchRequest(ByteBuf packet, Support support, Configuration config) {
        String pluginName = readCString(packet, UTF_8);
        if (pluginName == null)
            return Result.protocolErrorAbortEverything("Couldn't read pluginName during auth");
        byte[] pluginData = readFixedBytes(packet, packet.readableBytes());

        byte[] auth = generateAuth(pluginName, pluginData, config.password, false);
        if (auth == null) {
            return Result.protocolErrorAbortEverything("Unsupported authentication mechanism ("+pluginName+") requested by server");
        } else {
            support.sendMessage(new AuthenticationSwitchResponse(this, auth));
            return Result.expectingMorePackets();
        }
    }

    private HandshakeResponseMessage generateHandshakeResponse(String authenticationMethod, byte[] authSeed, Configuration config) {
        HandshakeResponseMessage response = new HandshakeResponseMessage(this, config.username);

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
