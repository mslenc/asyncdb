package com.github.mslenc.asyncdb.mysql.message.client;

import com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HandshakeResponseMessage extends ClientMessage {
    private int capabilityFlags = MySQLIO.CLIENT_PROTOCOL_41 |
                                  MySQLIO.CLIENT_TRANSACTIONS |
                                  // TODO - support multiple result sets per query, see https://dev.mysql.com/doc/internals/en/multi-resultset.html
                                  MySQLIO.CLIENT_MULTI_RESULTS |
                                  MySQLIO.CLIENT_SECURE_CONNECTION;

    private String username;

    private String authMethod;
    private byte[] authData;

    private String database;

    public HandshakeResponseMessage(String username) {
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
            this.capabilityFlags &= ~MySQLIO.CLIENT_CONNECT_WITH_DB;
        } else {
            this.database = database;
            this.capabilityFlags |= MySQLIO.CLIENT_CONNECT_WITH_DB;
        }
    }

    public void setAuthMethod(String authMethod, byte[] authData) {
        this.authMethod = Objects.requireNonNull(authMethod, "Missing authMethod");

        this.authData = Objects.requireNonNull(authData);
        if (authData.length > 255)
            throw new IllegalArgumentException("Invalid authData length (" + authData.length + ")");

        this.capabilityFlags |= MySQLIO.CLIENT_PLUGIN_AUTH;
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer();

        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse

        contents.writeIntLE(capabilityFlags);
        contents.writeIntLE(MySQLIO.MAX_PACKET_LENGTH);
        contents.writeByte(MySQLIO.CHARSET_ID_UTF8MB4);
        contents.writeZero(23);
        ByteBufUtils.writeCString(contents, username, UTF_8);

        if (authData != null) {
            contents.writeByte(authData.length);
            contents.writeBytes(authData);
        } else {
            contents.writeByte(0);
        }

        if (database != null) {
            ByteBufUtils.writeCString(contents, database, UTF_8);
        }

        if (authMethod != null) {
            ByteBufUtils.writeCString(contents, authMethod, UTF_8);
        }

        return contents;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "HandshakeResponse(TODO)";
    }
}
