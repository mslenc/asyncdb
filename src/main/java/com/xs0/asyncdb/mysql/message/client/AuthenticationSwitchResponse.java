package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

public class AuthenticationSwitchResponse implements ClientMessage {
    private final byte[] authData;
    private final int sequenceNumber;

    public AuthenticationSwitchResponse(byte[] authData, int sequenceNumber) {
        this.authData = authData;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public void encodeInto(ByteBuf packet) {
        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::AuthSwitchResponse
        packet.writeBytes(authData);
    }

    @Override
    public int packetSequenceNumber() {
        return sequenceNumber;
    }
}
