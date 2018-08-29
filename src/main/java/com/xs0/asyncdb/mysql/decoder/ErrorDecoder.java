package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.ErrorMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readFixedString;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readUntilEOF;

public class ErrorDecoder implements MessageDecoder {
    private final Charset charset;

    public ErrorDecoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ErrorMessage decode(ByteBuf buffer) {
        return new ErrorMessage(
            buffer.readShort(),
            readFixedString(buffer, 6, charset),
            readUntilEOF(buffer, charset)
        );
    }
}