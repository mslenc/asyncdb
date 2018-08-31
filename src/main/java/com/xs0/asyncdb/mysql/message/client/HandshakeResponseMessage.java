package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.writeCString;
import static com.xs0.asyncdb.mysql.util.MySQLIO.*;
import static com.xs0.asyncdb.mysql.util.MySQLIO.CLIENT_SECURE_CONNECTION;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HandshakeResponseMessage implements ClientMessage {
    private int capabilityFlags = CLIENT_PROTOCOL_41 |
                                  CLIENT_TRANSACTIONS |
                                  // TODO - support multiple result sets per query, see https://dev.mysql.com/doc/internals/en/multi-resultset.html
                                  // CLIENT_MULTI_RESULTS |
                                  CLIENT_SECURE_CONNECTION;

    private int charsetId = CharsetMapper.CHARSET_UTF8MB4_BIN;
    private int maxPacketSize = 0xFFFFFF;

    private String username;

    private String authMethod;
    private byte[] authData;

    private String database;

    public HandshakeResponseMessage(String username) {
        this.username = Objects.requireNonNull(username, "Missing username");
    }

    public void setDatabase(String database) {
        if (database == null || database.isEmpty()) {
            this.database = null;
            this.capabilityFlags &= ~CLIENT_CONNECT_WITH_DB;
        } else {
            this.database = database;
            this.capabilityFlags |= CLIENT_CONNECT_WITH_DB;
        }
    }

    public void setAuthMethod(String authMethod, byte[] authData) {
        this.authMethod = Objects.requireNonNull(authMethod, "Missing authMethod");

        this.authData = Objects.requireNonNull(authData);
        if (authData.length > 255)
            throw new IllegalArgumentException("Invalid authData length (" + authData.length + ")");

        this.capabilityFlags |= CLIENT_PLUGIN_AUTH;
    }

    @Override
    public void encodeInto(ByteBuf packet) {
        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse

        packet.writeIntLE(capabilityFlags);
        packet.writeIntLE(maxPacketSize);
        packet.writeByte(charsetId);
        packet.writeZero(23);
        writeCString(packet, username, UTF_8);

        if (authData != null) {
            packet.writeByte(authData.length);
            packet.writeBytes(authData);
        } else {
            packet.writeByte(0);
        }

        if (database != null) {
            writeCString(packet, database, UTF_8);
        }

        if (authMethod != null) {
            writeCString(packet, authMethod, UTF_8);
        }
    }

    @Override
    public int packetSequenceNumber() {
        return 1;
    }
}
