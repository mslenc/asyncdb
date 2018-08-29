package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.ParamAndColumnProcessingFinishedMessage;
import io.netty.buffer.ByteBuf;

public class ParamAndColumnProcessingFinishedDecoder implements MessageDecoder {
    private static final ParamAndColumnProcessingFinishedDecoder instance = new ParamAndColumnProcessingFinishedDecoder();

    public static ParamAndColumnProcessingFinishedDecoder instance() {
        return instance;
    }

    @Override
    public ParamAndColumnProcessingFinishedMessage decode(ByteBuf buffer) {
        return new ParamAndColumnProcessingFinishedMessage(EOFMessageDecoder.instance().decode(buffer));
    }
}
