package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.util.ByteBufUtils;
import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HandshakeResponseMessage extends ClientMessage {
    public static final int
        DEFAULT_CAPABILITY_FLAGS = MyConstants.CLIENT_PROTOCOL_41 |
                                   MyConstants.CLIENT_TRANSACTIONS |
                                   // TODO - support multiple result sets per query? see https://dev.mysql.com/doc/internals/en/multi-resultset.html
                                   MyConstants.CLIENT_MULTI_RESULTS |
                                   MyConstants.CLIENT_SECURE_CONNECTION;



    private int capabilityFlags = DEFAULT_CAPABILITY_FLAGS;

    private String username;

    private String authMethod;
    private byte[] authData;

    private String database;

    private boolean doingSsl = false;

    public HandshakeResponseMessage(String username) {
        this.username = Objects.requireNonNull(username, "Missing username");
    }

    @Override
    public int getFirstPacketSequenceNumber() {
        // initial handshake must start with sequence number 1, for some unknown reason...
        return doingSsl ? 2 : 1;
    }

    public void setDatabase(String database) {
        if (database == null || database.isEmpty()) {
            this.database = null;
            this.capabilityFlags &= ~MyConstants.CLIENT_CONNECT_WITH_DB;
        } else {
            this.database = database;
            this.capabilityFlags |= MyConstants.CLIENT_CONNECT_WITH_DB;
        }
    }

    public void setAuthMethod(String authMethod, byte[] authData) {
        this.authMethod = Objects.requireNonNull(authMethod, "Missing authMethod");

        this.authData = Objects.requireNonNull(authData);
        if (authData.length > 255)
            throw new IllegalArgumentException("Invalid authData length (" + authData.length + ")");

        this.capabilityFlags |= MyConstants.CLIENT_PLUGIN_AUTH;
    }

    public void setClientSsl(boolean useClientSsl) {
        if (useClientSsl) {
            this.capabilityFlags |= MyConstants.CLIENT_SSL;
        } else {
            this.capabilityFlags &= ~MyConstants.CLIENT_SSL;
        }
    }

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf contents = Unpooled.buffer();

        // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse

        contents.writeIntLE(capabilityFlags);
        contents.writeIntLE(MyConstants.MAX_PACKET_LENGTH);
        contents.writeByte(MyConstants.CHARSET_ID_UTF8MB4);
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

    public SSLRequestMessage makeSSLRequest() {
        doingSsl = true;
        return new SSLRequestMessage(capabilityFlags);
    }

    @Override
    public String toString(boolean fullDetails) {
        return "HandshakeResponse(TODO)";
    }
}
