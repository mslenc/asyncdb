package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.MyConstants;
import com.github.mslenc.asyncdb.my.auth.MyAuthCachingSha2;
import com.github.mslenc.asyncdb.my.auth.MyAuthNativePassword;
import com.github.mslenc.asyncdb.my.auth.MyAuthOldPassword;
import com.github.mslenc.asyncdb.my.msgclient.AuthenticationSwitchResponse;
import com.github.mslenc.asyncdb.my.msgclient.ChangeUserMessage;
import com.github.mslenc.asyncdb.my.msgclient.HandshakeResponseMessage;
import com.github.mslenc.asyncdb.my.msgserver.ErrorMessage;
import com.github.mslenc.asyncdb.my.msgserver.HandshakeMessage;
import com.github.mslenc.asyncdb.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.readCString;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readFixedBytes;
import static com.github.mslenc.asyncdb.my.MyConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MyHandshakeCmd extends MyCommand {
    private static final Logger log = LoggerFactory.getLogger(MyHandshakeCmd.class);

    private static final int STATE_INITIAL = 0;
    private static final int STATE_CLIENT_HANDSHAKE_SENT = 1;
    private static final int STATE_CHANGE_USER = 2;
    private static final int STATE_WAITING_SSL = 3;
    private static final int STATE_AWAITING_OK = 4;

    private final CompletableFuture<MyConnection> promise;
    private final String username;
    private final String password;
    private final String database;

    private int state;
    private int authSwitchSeqNumber;
    private boolean haveSsl = false;

    public MyHandshakeCmd(MyConnection conn, String username, String password, String database, boolean changeUser, CompletableFuture<MyConnection> promise) {
        super(conn);

        this.username = username;
        this.password = password;
        this.database = database;
        this.promise = promise;

        if (changeUser) {
            state = STATE_CHANGE_USER;
            authSwitchSeqNumber = 2;
        } else {
            state = STATE_INITIAL;
            authSwitchSeqNumber = 2;
        }
    }

    public CompletableFuture<MyConnection> getPromise() {
        return promise;
    }

    @Override
    public Result start() {
        if (state == STATE_INITIAL)
            return Result.expectingMorePackets();

        ChangeUserMessage msg = new ChangeUserMessage(username);
        msg.setAuthMethod(conn.serverInfo().authenticationMethod, generateAuth(conn.serverInfo().authenticationMethod, conn.serverInfo().seed, password, false));
        msg.setDatabase(database);
        conn.sendMessage(msg);
        state = STATE_CLIENT_HANDSHAKE_SENT;
        return Result.expectingMorePackets();
    }

    @Override
    public Result processPacket(ByteBuf packet) {
        switch (state) {
            case STATE_INITIAL:
                return processPacket_INITIAL(packet);

            case STATE_CLIENT_HANDSHAKE_SENT:
                return processPacket_HANDSHAKE_SENT(packet);

            case STATE_AWAITING_OK:
                return processPacket_AWAITING_OK(packet);

            default:
                throw new IllegalStateException("Invalid state");
        }
    }

    private Result processPacket_INITIAL(ByteBuf packet) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_ERR:
                ErrorMessage error = ErrorMessage.decodeAfterHeader(packet, StandardCharsets.ISO_8859_1, 0);
                return Result.protocolErrorAbortEverything(error.errorMessage);

            case PACKET_HEADER_HANDSHAKE_V10:
                HandshakeMessage handshake = HandshakeMessage.decodeAfterHeader(packet);
                conn.setServerInfo(handshake);

                HandshakeResponseMessage response = generateHandshakeResponse(handshake.authenticationMethod, handshake.seed);

                if ((handshake.serverCapabilities & MyConstants.CLIENT_SSL) != 0 && conn.isSslSupported()) {
                    state = STATE_WAITING_SSL;

                    CompletableFuture<Void> future = new CompletableFuture<>();
                    future.whenComplete((unused, throwable) -> {
                        if (throwable != null) {
                            conn.handleException(throwable);
                        } else {
                            conn.sendMessage(response);
                            haveSsl = true;
                            state = STATE_CLIENT_HANDSHAKE_SENT;
                        }
                    });

                    return Result.switchToSsl(response.makeSSLRequest(), future);
                } else {
                    conn.sendMessage(response);
                    state = STATE_CLIENT_HANDSHAKE_SENT;
                    return Result.expectingMorePackets();
                }

            default:
                return Result.unknownHeaderByte(header, "handshake (initial)");
        }
    }

    private Result processPacket_HANDSHAKE_SENT(ByteBuf packet) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_OK:
                log.debug("Authentication successful");
                promise.complete(conn);
                return Result.stateMachineFinished();

            case PACKET_HEADER_AUTH_SWITCH_REQUEST:
                return respondToAuthSwitchRequest(packet);

            case PACKET_HEADER_ERR:
                ErrorMessage error = ErrorMessage.decodeAfterHeader(packet, UTF_8, conn.serverInfo().serverCapabilities);
                return Result.protocolErrorAbortEverything(error.errorMessage);

            case PACKET_HEADER_AUTH_MORE_DATA:
                if (packet.readableBytes() == 1) {
                    byte b = packet.readByte();
                    switch (b) {
                        case 3:
                            log.debug("Fast auth ok");
                            state = STATE_AWAITING_OK;
                            return Result.expectingMorePackets();

                        case 4:
                            log.debug("Fast auth not ok");
                            if (haveSsl) {
                                conn.sendMessage(new AuthenticationSwitchResponse(4, zeroTerminatedString(password)));
                                state = STATE_AWAITING_OK;
                                return Result.expectingMorePackets();
                            } else {
                                return Result.protocolErrorAbortEverything("Don't have SSL and we don't support encoding passwords (after server didn't want fast auth).");
                            }

                        default:
                            return Result.protocolErrorAbortEverything("Didn't receive 3 or 4 during auth.");
                    }
                }

            default:
                return Result.unknownHeaderByte(header, "handshake (later)");
        }
    }

    private static byte[] zeroTerminatedString(String s) {
        byte[] utf8 = s.getBytes(UTF_8);
        byte[] result = new byte[utf8.length + 1];
        System.arraycopy(utf8, 0, result, 0, utf8.length);
        return result;
    }

    private Result processPacket_AWAITING_OK(ByteBuf packet) {
        int header = consumePacketHeader(packet);
        switch (header) {
            case PACKET_HEADER_OK:
                log.debug("Authentication successful.");
                promise.complete(conn);
                return Result.stateMachineFinished();

            case PACKET_HEADER_ERR:
                ErrorMessage error = ErrorMessage.decodeAfterHeader(packet, UTF_8, conn.serverInfo().serverCapabilities);
                return Result.protocolErrorAbortEverything(error.errorMessage);

            default:
                return Result.unknownHeaderByte(header, "handshake (after auth success)");
        }
    }

    private Result respondToAuthSwitchRequest(ByteBuf packet) {
        String pluginName = readCString(packet, UTF_8);
        if (pluginName == null)
            return Result.protocolErrorAbortEverything("Couldn't read pluginName during auth");
        byte[] pluginData = readFixedBytes(packet, packet.readableBytes());

        byte[] auth = generateAuth(pluginName, pluginData, password, false);
        if (auth == null) {
            return Result.protocolErrorAbortEverything("Unsupported authentication mechanism ("+pluginName+") requested by server");
        } else {
            conn.sendMessage(new AuthenticationSwitchResponse(authSwitchSeqNumber++, auth));
            return Result.expectingMorePackets();
        }
    }

    private HandshakeResponseMessage generateHandshakeResponse(String authenticationMethod, byte[] authSeed) {
        HandshakeResponseMessage response = new HandshakeResponseMessage(username);
        response.setClientSsl(conn.isSslSupported());

        if (authenticationMethod != null)
            response.setAuthMethod(authenticationMethod, generateAuth(authenticationMethod, authSeed, password, true));

        if (database != null)
            response.setDatabase(database);

        return response;
    }

    static byte[] generateAuth(String authenticationMethod, byte[] authSeed, String password, boolean defaultUnrecognizedToNative) {
        switch (authenticationMethod) {
            case AUTH_OLD:
                return MyAuthOldPassword.generateAuthentication(UTF_8, password, authSeed);

            default:
                if (!defaultUnrecognizedToNative)
                    return null;
                // else fall through

            case AUTH_NATIVE:
                return MyAuthNativePassword.generateAuthentication(UTF_8, password, authSeed);

            case AUTH_CACHING_SHA2:
                return MyAuthCachingSha2.generateAuthentication(UTF_8, password, authSeed);
        }
    }
}
