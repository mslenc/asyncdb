package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.ColumnProcessingFinishedMessage;
import io.netty.buffer.ByteBuf;

public class ColumnProcessingFinishedDecoder implements MessageDecoder {
    private static final ColumnProcessingFinishedDecoder instance = new ColumnProcessingFinishedDecoder();

    public static ColumnProcessingFinishedDecoder instance() {
        return instance;
    }

    @Override
    public ColumnProcessingFinishedMessage decode(ByteBuf buffer) {
        return new ColumnProcessingFinishedMessage(EOFMessageDecoder.instance().decode(buffer));
    }
}
