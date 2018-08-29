package com.xs0.asyncdb.mysql.encoder;

import com.xs0.asyncdb.common.exceptions.UnsupportedAuthenticationMethodException;
import com.xs0.asyncdb.mysql.encoder.auth.AuthenticationMethod;
import com.xs0.asyncdb.mysql.message.client.AuthenticationSwitchResponse;
import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.newPacketBuffer;

public class AuthenticationSwitchResponseEncoder implements MessageEncoder {
    private final Charset charset;

    public AuthenticationSwitchResponseEncoder(Charset charset) {
        this.charset = charset;
    }

    public ByteBuf encode(ClientMessage message) {
        AuthenticationSwitchResponse switchRes = (AuthenticationSwitchResponse) message;

        String method = switchRes.request.method;
        AuthenticationMethod authenticator = AuthenticationMethod.byName(method);
        if (authenticator == null)
            throw new UnsupportedAuthenticationMethodException(method);

        ByteBuf buffer = newPacketBuffer(50);

        byte[] bytes = authenticator.generateAuthentication(charset, switchRes.password, switchRes.request.seed.getBytes(charset));
        buffer.writeBytes(bytes);

        return buffer;
    }
}
