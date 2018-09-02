package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AuthenticationSwitchResponse extends ClientMessage {
    private final byte[] authData;

    public AuthenticationSwitchResponse(MySQLCommand command, byte[] authData) {
        super(command);

        this.authData = authData;
    }

    @Override
    public ByteBuf getPacketContents() {
        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::AuthSwitchResponse

        return Unpooled.wrappedBuffer(authData);
    }
}
