package com.xs0.asyncdb.mysql.codec;

import java.nio.charset.Charset;
import java.util.List;

import com.xs0.asyncdb.common.exceptions.EncoderNotAvailableException;
import com.xs0.asyncdb.common.util.BufferDumper;
import com.xs0.asyncdb.mysql.binary.BinaryRowEncoder;
import com.xs0.asyncdb.mysql.encoder.*;
import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.writePacketLength;
import static com.xs0.asyncdb.mysql.message.client.ClientMessage.*;

public class MySQLOneToOneEncoder extends MessageToMessageEncoder<ClientMessage> {
    private static final Logger log = LoggerFactory.getLogger(MySQLOneToOneEncoder.class);

    private final HandshakeResponseEncoder handshakeResponseEncoder;
    private final QueryMessageEncoder queryEncoder;
    private final PreparedStatementPrepareEncoder prepareEncoder;
    private final PreparedStatementExecuteEncoder executeEncoder;
    private final AuthenticationSwitchResponseEncoder authenticationSwitchEncoder;

    private int sequence = 1;

    public MySQLOneToOneEncoder(Charset charset, CharsetMapper charsetMapper) {
        super(ClientMessage.class);

        handshakeResponseEncoder = new HandshakeResponseEncoder(charset, charsetMapper);
        queryEncoder = new QueryMessageEncoder(charset);
        prepareEncoder = new PreparedStatementPrepareEncoder(charset);
        executeEncoder = new PreparedStatementExecuteEncoder(new BinaryRowEncoder(charset));
        authenticationSwitchEncoder = new AuthenticationSwitchResponseEncoder(charset);
    }


    public void encode(ChannelHandlerContext ctx, ClientMessage message, List<Object> out) {
        final MessageEncoder encoder;

        switch (message.kind()) {
            case CLIENT_PROTOCOL_VERSION:
                encoder = handshakeResponseEncoder;
                break;

            case QUIT:
                sequence = 0;
                encoder = QuitMessageEncoder.instance();
                break;

            case QUERY:
                sequence = 0;
                encoder = queryEncoder;
                break;

            case PREPARED_STATEMENT_EXECUTE:
                sequence = 0;
                encoder = executeEncoder;
                break;

            case PREPARED_STATEMENT_PREPARE:
                sequence = 0;
                encoder = prepareEncoder;
                break;

            case AUTH_SWITCH_RESPONSE:
                sequence++;
                encoder = authenticationSwitchEncoder;
                break;

            default:
                throw new EncoderNotAvailableException(message);

        }

        ByteBuf result = encoder.encode(message);

        writePacketLength(result, sequence);
        sequence++;

        if (log.isTraceEnabled()) {
            log.trace("Writing message {} - \n{}", message.getClass().getName(), BufferDumper.dumpAsHex(result));
        }

        out.add(result);
    }
}
