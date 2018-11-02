package com.github.mslenc.asyncdb.ex;

import com.github.mslenc.asyncdb.util.BufferDumper;
import io.netty.buffer.ByteBuf;

public class BufferNotFullyConsumedException extends DatabaseException {
    public BufferNotFullyConsumedException(ByteBuf buffer) {
        super("Buffer was not fully consumed by decoder, " + buffer.readableBytes() + " bytes remained");

        buffer.markReaderIndex();
        buffer.readerIndex(0);
        System.err.println(BufferDumper.dumpAsHex(buffer));
        buffer.resetReaderIndex();
    }
}
