package com.xs0.asyncdb.mysql.message.client;

import java.nio.charset.Charset;

public class HandshakeResponseMessage implements ClientMessage {
    public final String username;
    public final Charset charset;
    public final byte[] seed;
    public final String authenticationMethod;
    public final String password;
    public final String database;

    public HandshakeResponseMessage(String username, Charset charset, byte[] seed, String authenticationMethod, String password, String database) {
        this.username = username;
        this.charset = charset;
        this.seed = seed;
        this.authenticationMethod = authenticationMethod;
        this.password = password;
        this.database = database;
    }

    @Override
    public int kind() {
        return CLIENT_PROTOCOL_VERSION;
    }
}
