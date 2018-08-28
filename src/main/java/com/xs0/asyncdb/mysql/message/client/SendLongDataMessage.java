package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

public class SendLongDataMessage {
    public final byte[] statementId;
    public final ByteBuf value;
    public final int paramId;

    public SendLongDataMessage(byte[] statementId, ByteBuf value, int paramId) {
        this.statementId = statementId;
        this.value = value;
        this.paramId = paramId;
    }
}
