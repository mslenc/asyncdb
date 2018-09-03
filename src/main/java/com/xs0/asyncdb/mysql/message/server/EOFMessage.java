package com.xs0.asyncdb.mysql.message.server;

public class EOFMessage implements ServerMessage {
    public final int warningCount;
    public final int flags;

    public EOFMessage(int warningCount, int flags) {
        this.warningCount = warningCount;
        this.flags = flags;
    }
}
