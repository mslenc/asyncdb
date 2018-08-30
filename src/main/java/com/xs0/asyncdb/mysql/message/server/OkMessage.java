package com.xs0.asyncdb.mysql.message.server;

public class OkMessage implements ServerMessage {
    public final long affectedRows;
    public final long lastInsertId;
    public final int statusFlags;
    public final int warnings;
    public final String message;

    public OkMessage(long affectedRows, long lastInsertId, int statusFlags, int warnings, String message) {
        this.affectedRows = affectedRows;
        this.lastInsertId = lastInsertId;
        this.statusFlags = statusFlags;
        this.warnings = warnings;
        this.message = message;
    }

    @Override
    public int kind() {
        return OK;
    }
}
