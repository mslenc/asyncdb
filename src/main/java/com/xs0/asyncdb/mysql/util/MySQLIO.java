package com.xs0.asyncdb.mysql.util;

import io.netty.buffer.ByteBuf;

public class MySQLIO {
    public static final int MAX_PACKET_LENGTH = 0xFFFFFF; // (16777215)

    public static final int CLIENT_PROTOCOL_41 = 0x0200;
    public static final int CLIENT_CONNECT_WITH_DB = 0x8;
    public static final int CLIENT_TRANSACTIONS = 0x2000;
    public static final int CLIENT_MULTI_RESULTS = 0x20000;
    public static final int CLIENT_LONG_FLAG = 0x1;
    public static final int CLIENT_PLUGIN_AUTH = 0x80000;
    public static final int CLIENT_SECURE_CONNECTION = 0x8000;

    public static final int NO_PACKET_HEADER = Integer.MIN_VALUE;

    public static final int PACKET_HEADER_OK = 0x00;
    public static final int PACKET_HEADER_QUIT = 0x01;
    public static final int PACKET_HEDAER_COM_QUERY = 0x03;
    public static final int PACKET_HEADER_STMT_PREPARE = 0x16;
    public static final int PACKET_HEADER_STMT_EXECUTE = 0x17;
    public static final int PACKET_HEADER_STMT_SEND_LONG_DATA = 0x18;
    public static final int PACKET_HEADER_STMT_CLOSE = 0x19;
    public static final int PACKET_HEADER_STMT_RESET = 0x1A;
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

    public static boolean isOKPacket(ByteBuf packet) {
        return packet.readableBytes() > 7 && packet.getUnsignedByte(packet.readerIndex()) == PACKET_HEADER_OK;
    }

    public static boolean isEOFPacket(ByteBuf packet) {
        return packet.readableBytes() < 9 && packet.readableBytes() > 0 && packet.getUnsignedByte(packet.readerIndex()) == PACKET_HEADER_EOF;
    }

    public static boolean isERRPacket(ByteBuf packet) {
        return packet.readableBytes() > 0 && packet.getUnsignedByte(packet.readerIndex()) == PACKET_HEADER_ERR;
    }
}
