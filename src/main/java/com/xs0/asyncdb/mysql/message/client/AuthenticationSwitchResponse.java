package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.message.server.AuthenticationSwitchRequest;

public class AuthenticationSwitchResponse implements ClientMessage {
    public final String password;
    public final AuthenticationSwitchRequest request;

    public AuthenticationSwitchResponse(String password, AuthenticationSwitchRequest request) {
        this.password = password;
        this.request = request;
    }

    @Override
    public int kind() {
        return AuthSwitchResponse;
    }
}
