package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.OkMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readBinaryLength;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readUntilEOF;

public class OkDecoder implements MessageDecoder {
    private final Charset charset;

    public OkDecoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public OkMessage decode(ByteBuf buffer) {
        return new OkMessage(
            readBinaryLength(buffer),
            readBinaryLength(buffer),
            buffer.readShort(),
            buffer.readShort(),
            readUntilEOF(buffer, charset)
        );
    }
}