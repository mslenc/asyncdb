package com.xs0.asyncdb.mysql.message.server;

public class ParamAndColumnProcessingFinishedMessage implements ServerMessage {
    public final EOFMessage eofMessage;

    public ParamAndColumnProcessingFinishedMessage(EOFMessage eofMessage) {
        this.eofMessage = eofMessage;
    }

    @Override
    public int kind() {
        return PARAM_AND_COLUMN_PROCESSING_FINISHED;
    }
}
