package com.xs0.asyncdb.mysql.util;

import io.netty.buffer.ByteBuf;

public class MySQLIO {
    public static final int CLIENT_PROTOCOL_41 = 0x0200;
    public static final int CLIENT_CONNECT_WITH_DB = 0x0008;
    public static final int CLIENT_TRANSACTIONS = 0x2000;
    public static final int CLIENT_MULTI_RESULTS = 0x20000;
    public static final int CLIENT_LONG_FLAG = 0x0001;
    public static final int CLIENT_PLUGIN_AUTH = 0x00080000;
    public static final int CLIENT_SECURE_CONNECTION = 0x00008000;

    public static final int NO_PACKET_HEADER = Integer.MIN_VALUE;
    public static final int PACKET_HEADER_OK = 0x00;
    public static final int PACKET_HEDAER_COM_QUERY = 0x03;
    public static final int PACKET_HEADER_STMT_PREPARE = 0x16;
    public static final int PACKET_HEADER_EOF = 0xFE;
    public static final int PACKET_HEADER_ERR = 0xFF;
    public static final int PACKET_HEADER_GET_MORE_CLIENT_DATA = 0xFB;
    public static final int PACKET_HEADER_HANDSHAKE_V10 = 0x0A;
    public static final int PACKET_HEADER_AUTH_SWITCH_REQUEST = 0xFE;

    public static final int TEXT_RESULTSET_NULL = 0xfb;

    public static final String AUTH_NATIVE = "mysql_native_password";
    public static final String AUTH_OLD = "mysql_old_password";

    public static int consumePacketHeader(ByteBuf packet) {
        if (packet.readableBytes() >= 1) {
            return packet.readUnsignedByte();
        } else {
            return NO_PACKET_HEADER;
        }
    }

}
