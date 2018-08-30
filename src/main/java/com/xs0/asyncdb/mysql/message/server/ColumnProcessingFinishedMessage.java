package com.xs0.asyncdb.mysql.message.server;

public class ColumnProcessingFinishedMessage implements ServerMessage {
    public final EOFMessage eofMessage;

    public ColumnProcessingFinishedMessage(EOFMessage eofMessage) {
        this.eofMessage = eofMessage;
    }

    @Override
    public int kind() {
        return COLUMN_DEFINITION_FINISHED;
    }
}
