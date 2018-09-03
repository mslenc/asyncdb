package com.xs0.asyncdb.mysql.message.server;

public class AuthenticationSwitchRequest implements ServerMessage {
    public final String method;
    public final String seed;

    public AuthenticationSwitchRequest(String method, String seed) {
        this.method = method;
        this.seed = seed;
    }
}
