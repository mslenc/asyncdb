package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.state.MySQLCommand;
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
}
