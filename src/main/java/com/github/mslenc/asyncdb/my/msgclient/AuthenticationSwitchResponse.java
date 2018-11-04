package com.github.mslenc.asyncdb.my.msgclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AuthenticationSwitchResponse extends ClientMessage {
    private final int seqNumber;
    private final byte[] authData;

    public AuthenticationSwitchResponse(int seqNumber, byte[] authData) {
        this.seqNumber = seqNumber;
        this.authData = authData;
    }

    @Override
    public int getFirstPacketSequenceNumber() {
        return seqNumber;
    }

    @Override
    public ByteBuf getPacketContents() {
        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::AuthSwitchResponse

        return Unpooled.wrappedBuffer(authData);
    }

    @Override
    public String toString(boolean fullDetails) {
        return "AuthSwitchResponse";
    }
}
