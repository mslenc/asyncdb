package com.xs0.asyncdb.mysql.message.server;

public class HandshakeMessage implements ServerMessage {
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
    public int kind() {
        return SERVER_PROTOCOL_VERSION;
    }
}
