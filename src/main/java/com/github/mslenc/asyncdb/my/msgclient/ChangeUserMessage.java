package com.github.mslenc.asyncdb.my.msgclient;

import com.github.mslenc.asyncdb.my.MyConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.writeCString;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ChangeUserMessage extends ClientMessage {
    private int capabilityFlags = HandshakeResponseMessage.DEFAULT_CAPABILITY_FLAGS;

    private String username;

    private String authMethod;
    private byte[] authData;

    private String database;

    public ChangeUserMessage(String username) {
        this.username = Objects.requireNonNull(username, "Missing username");
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

    @Override
    public ByteBuf getPacketContents() {
        ByteBuf packet = Unpooled.buffer();

        // https://dev.mysql.com/doc/internals/en/com-change-user.html

        packet.writeByte(MyConstants.PACKET_HEADER_CHANGE_USER);
        writeCString(packet, username, UTF_8);
        packet.writeByte(authData.length);
        packet.writeBytes(authData);
        writeCString(packet, database != null ? database : "", UTF_8);
        packet.writeShortLE(MyConstants.CHARSET_ID_UTF8MB4);
        writeCString(packet, authMethod, UTF_8);

        return packet;
    }

    @Override
    public String toString(boolean fullDetails) {
        return "ChangeUserMessage(TODO)";
    }
}
