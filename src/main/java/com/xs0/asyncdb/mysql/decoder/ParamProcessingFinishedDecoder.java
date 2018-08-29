package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.ParamProcessingFinishedMessage;
import io.netty.buffer.ByteBuf;

public class ParamProcessingFinishedDecoder implements MessageDecoder {
    private static final ParamProcessingFinishedDecoder instance = new ParamProcessingFinishedDecoder();

    public static ParamProcessingFinishedDecoder instance() {
        return instance;
    }

    @Override
    public ParamProcessingFinishedMessage decode(ByteBuf buffer) {
        return new ParamProcessingFinishedMessage(EOFMessageDecoder.instance().decode(buffer));
    }
}
