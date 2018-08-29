package com.xs0.asyncdb.mysql.encoder;

import java.nio.charset.Charset;

import com.xs0.asyncdb.mysql.binary.ByteBufUtils;
import com.xs0.asyncdb.mysql.encoder.auth.AuthenticationMethod;
import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import com.xs0.asyncdb.mysql.message.client.HandshakeResponseMessage;
import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.writeCString;
import static com.xs0.asyncdb.mysql.util.MySQLIO.*;

public class HandshakeResponseEncoder implements MessageEncoder {
    private static final int MAX_3_BYTES = 0x00ffffff;
    private static final byte[] PADDING = new byte[23];
    private static final Logger log = LoggerFactory.getLogger(HandshakeResponseEncoder.class);

    private final Charset charset;
    private final CharsetMapper charsetMapper;

    public HandshakeResponseEncoder(Charset charset, CharsetMapper charsetMapper) {
        this.charset = charset;
        this.charsetMapper = charsetMapper;
    }

    @Override
    public ByteBuf encode(ClientMessage message) {
        HandshakeResponseMessage m = (HandshakeResponseMessage) message;


        int clientCapabilities =
                CLIENT_PLUGIN_AUTH |
                CLIENT_PROTOCOL_41 |
                CLIENT_TRANSACTIONS |
                CLIENT_MULTI_RESULTS |
                CLIENT_SECURE_CONNECTION;

        if (m.database != null) {
            clientCapabilities |= CLIENT_CONNECT_WITH_DB;
        }

        ByteBuf buffer = ByteBufUtils.newPacketBuffer();

        buffer.writeInt(clientCapabilities);
        buffer.writeInt(MAX_3_BYTES);
        buffer.writeByte(charsetMapper.toInt(charset));
        buffer.writeBytes(PADDING);
        writeCString(buffer, m.username, charset);

        if (m.password != null) {
            String method = m.authenticationMethod;
            AuthenticationMethod authenticator = AuthenticationMethod.byName(method);
            byte[] bytes = authenticator.generateAuthentication(charset, m.password, m.seed);
            buffer.writeByte(bytes.length);
            buffer.writeBytes(bytes);
        } else {
            buffer.writeByte(0);
        }

        if (m.database != null) {
            writeCString(buffer, m.database, charset);
        }

        writeCString(buffer, m.authenticationMethod, charset);

        return buffer;
    }
}
