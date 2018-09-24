package com.github.mslenc.asyncdb.mysql.decoder;

import java.nio.charset.Charset;

import com.github.mslenc.asyncdb.mysql.message.server.AuthenticationSwitchRequest;
import com.github.mslenc.asyncdb.mysql.message.server.ServerMessage;
import io.netty.buffer.ByteBuf;

import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.readCString;
import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.readUntilEOF;

public class AuthenticationSwitchRequestDecoder implements MessageDecoder {
    private final Charset charset;

    public AuthenticationSwitchRequestDecoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ServerMessage decode(ByteBuf buffer) {
        return new AuthenticationSwitchRequest(
            readCString(buffer, charset),
            readUntilEOF(buffer, charset)
        );
    }
}
