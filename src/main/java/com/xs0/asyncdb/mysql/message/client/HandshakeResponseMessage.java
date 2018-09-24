package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.mysql.state.MySQLCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.writeCString;
import static com.xs0.asyncdb.mysql.util.MySQLIO.*;
import static com.xs0.asyncdb.mysql.util.MySQLIO.CLIENT_SECURE_CONNECTION;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HandshakeResponseMessage extends ClientMessage {
    private int capabilityFlags = CLIENT_PROTOCOL_41 |
                                  CLIENT_TRANSACTIONS |
                                  // TODO - support multiple result sets per query, see https://dev.mysql.com/doc/internals/en/multi-resultset.html
                                  CLIENT_MULTI_RESULTS |
                                  CLIENT_SECURE_CONNECTION;

    private String username;

    private String authMethod;
    private byte[] authData;

    private String database;

    public HandshakeResponseMessage(MySQLCommand command, String username) {
        super(command);

        this.username = Objects.requireNonNull(username, "Missing username");
    }

    @Override
    public int getFirstPacketSequenceNumber() {
        // initial handshake must start with sequence number 1, for some unknown reason...
        return 1;
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
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer();

        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse

        contents.writeIntLE(capabilityFlags);
        contents.writeIntLE(MAX_PACKET_LENGTH);
        contents.writeByte(CHARSET_ID_UTF8MB4);
        contents.writeZero(23);
        writeCString(contents, username, UTF_8);

        if (authData != null) {
            contents.writeByte(authData.length);
            contents.writeBytes(authData);
        } else {
            contents.writeByte(0);
        }

        if (database != null) {
            writeCString(contents, database, UTF_8);
        }

        if (authMethod != null) {
            writeCString(contents, authMethod, UTF_8);
        }

        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "HandshakeResponse(TODO)";
    }
}
