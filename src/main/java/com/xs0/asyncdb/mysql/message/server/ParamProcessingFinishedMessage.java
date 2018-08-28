package com.xs0.asyncdb.mysql.message.server;

public class ParamProcessingFinishedMessage implements ServerMessage {
    public final EOFMessage eofMessage;

    public ParamProcessingFinishedMessage(EOFMessage eofMessage) {
        this.eofMessage = eofMessage;
    }

    @Override
    public int kind() {
        return ParamProcessingFinished;
    }
}
