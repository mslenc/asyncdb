package com.github.mslenc.asyncdb.my;

import io.netty.buffer.ByteBuf;

public class MyConstants {
    public static final int MAX_PACKET_LENGTH = 0xFFFFFF; // (16777215)

    public static final int CLIENT_PROTOCOL_41 = 0x0200;
    public static final int CLIENT_CONNECT_WITH_DB = 0x8;
    public static final int CLIENT_TRANSACTIONS = 0x2000;
    public static final int CLIENT_MULTI_RESULTS = 0x20000;
    public static final int CLIENT_LONG_FLAG = 0x1;
    public static final int CLIENT_PLUGIN_AUTH = 0x80000;
    public static final int CLIENT_SECURE_CONNECTION = 0x8000; // deprecated
    public static final int CLIENT_SSL = 0x800;

    public static final int NO_PACKET_HEADER = Integer.MIN_VALUE;

    public static final int PACKET_HEADER_OK = 0x00;
    public static final int PACKET_HEADER_AUTH_MORE_DATA = 0x01;
    public static final int PACKET_HEADER_INIT_DB = 0x02;
    public static final int PACKET_HEDAER_COM_QUERY = 0x03;
    public static final int PACKET_HEADER_CHANGE_USER = 0x11;
    public static final int PACKET_HEADER_STMT_PREPARE = 0x16;
    public static final int PACKET_HEADER_STMT_EXECUTE = 0x17;
    public static final int PACKET_HEADER_STMT_SEND_LONG_DATA = 0x18;
    public static final int PACKET_HEADER_STMT_CLOSE = 0x19;
    public static final int PACKET_HEADER_STMT_RESET = 0x1A;
    public static final int PACKET_HEADER_RESET_CONNECTION = 0x1F;
    public static final int PACKET_HEADER_EOF = 0xFE;
    public static final int PACKET_HEADER_ERR = 0xFF;
    public static final int PACKET_HEADER_GET_MORE_CLIENT_DATA = 0xFB;
    public static final int PACKET_HEADER_HANDSHAKE_V10 = 0x0A;
    public static final int PACKET_HEADER_AUTH_SWITCH_REQUEST = 0xFE;

    // https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__column__definition__flags.html
    public static final int FIELD_FLAG_NOT_NULL = 1;
    public static final int FIELD_FLAG_PRIMARY_KEY = 2;
    public static final int FIELD_FLAG_UNIQUE_KEY = 4;
    public static final int FIELD_FLAG_MULTIPLE_KEY = 8;
    public static final int FIELD_FLAG_BLOB = 16;
    public static final int FIELD_FLAG_UNSIGNED = 32;
    public static final int FIELD_FLAG_ZEROFILL = 64;
    public static final int FIELD_FLAG_BINARY = 128;
    public static final int FIELD_FLAG_ENUM = 256;
    public static final int FIELD_FLAG_AUTO_INCREMENT = 512;
    public static final int FIELD_FLAG_TIMESTAMP = 1024;
    public static final int FIELD_FLAG_SET = 2048;
    public static final int FIELD_FLAG_NO_DEFAULT_VALUE = 4096;
    public static final int FIELD_FLAG_ON_UPDATE_NOW = 8192;
    public static final int FIELD_FLAG_NUM = 32768;

    public static final int PREP_STMT_UNSIGNED_FLAG = 0x8000; // see https://dev.mysql.com/doc/internals/en/com-stmt-execute.html

    public static final int CHARSET_ID_UTF8MB4 = 45;
    public static final int CHARSET_ID_BINARY = 63;


    // see https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-COM_QUERY_Response
    // see https://github.com/mysql/mysql-connector-j/blob/9cc87a48e75c2d2e87c1a293b2862ce651cb256e/src/com/mysql/jdbc/MysqlDefs.java
    public static final int FIELD_TYPE_DECIMAL = 0x00;
    public static final int FIELD_TYPE_TINY = 0x01;
    public static final int FIELD_TYPE_SHORT = 0x02;
    public static final int FIELD_TYPE_LONG = 0x03;
    public static final int FIELD_TYPE_FLOAT = 0x04;
    public static final int FIELD_TYPE_DOUBLE = 0x05;
    public static final int FIELD_TYPE_NULL = 0x06;
    public static final int FIELD_TYPE_TIMESTAMP = 0x07;
    public static final int FIELD_TYPE_LONGLONG = 0x08;
    public static final int FIELD_TYPE_INT24 = 0x09;
    public static final int FIELD_TYPE_DATE = 0x0A;
    public static final int FIELD_TYPE_TIME = 0x0B;
    public static final int FIELD_TYPE_DATETIME = 0x0C;
    public static final int FIELD_TYPE_YEAR = 0x0D;
    public static final int FIELD_TYPE_VARCHAR = 0x0F;
    public static final int FIELD_TYPE_BIT = 0x10;
    public static final int FIELD_TYPE_JSON = 0xF5;
    public static final int FIELD_TYPE_NEW_DECIMAL = 0xF6;
    public static final int FIELD_TYPE_ENUM = 0xF7;
    public static final int FIELD_TYPE_SET = 0xF8;
    public static final int FIELD_TYPE_TINY_BLOB = 0xF9;
    public static final int FIELD_TYPE_MEDIUM_BLOB = 0xFA;
    public static final int FIELD_TYPE_LONG_BLOB = 0xFB;
    public static final int FIELD_TYPE_BLOB = 0xFC;
    public static final int FIELD_TYPE_VAR_STRING = 0xFD;
    public static final int FIELD_TYPE_STRING = 0xFE;
    public static final int FIELD_TYPE_GEOMETRY = 0xFF;
    // these are server-internal..
    // public static final int FIELD_TYPE_NEWDATE = 0x0E;
    // public static final int FIELD_TYPE_TIMESTAMP2 = 0x11;
    // public static final int FIELD_TYPE_DATETIME2 = 0x12;
    // public static final int FIELD_TYPE_TIME2 = 0x13;


    public static final int TEXT_RESULTSET_NULL = 0xfb;

    public static final String AUTH_NATIVE = "mysql_native_password";
    public static final String AUTH_OLD = "mysql_old_password";
    public static final String AUTH_CACHING_SHA2 = "caching_sha2_password";

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
