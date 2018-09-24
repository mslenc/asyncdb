package com.github.mslenc.asyncdb.mysql.message.client;

import com.github.mslenc.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;

public abstract class ClientMessage {
    private final MySQLCommand command;

    protected ClientMessage(MySQLCommand command) {
        this.command = command;
    }

    public final MySQLCommand getCommand() {
        return command;
    }

    public abstract ByteBuf getPacketContents();

    public int getFirstPacketSequenceNumber() {
        return 0;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public abstract String toString(boolean fullDetails);
}
