package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

public abstract class ClientMessage {
    private boolean hasSequenceNumber;
    private int sequenceNumber;

    public final void assignPacketSequenceNumber(int sequenceNumber) {
        if (hasSequenceNumber) {
            throw new IllegalStateException("Sequence number already assigned");
        } else {
            hasSequenceNumber = true;
            this.sequenceNumber = sequenceNumber;
        }
    }

    public final int packetSequenceNumber() {
        if (hasSequenceNumber) {
            return sequenceNumber;
        } else {
            throw new IllegalStateException("Missing sequence number");
        }
    }

    public abstract ByteBuf getPacketContents();
}
