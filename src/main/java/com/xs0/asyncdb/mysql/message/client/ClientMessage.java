package com.xs0.asyncdb.mysql.message.client;

import io.netty.buffer.ByteBuf;

public interface ClientMessage {
    int CLIENT_PROTOCOL_VERSION = 0x09; // COM_STATISTICS
    int QUIT = 0x01; // COM_QUIT
    int QUERY = 0x03; // COM_QUERY
    int PREPARED_STATEMENT_PREPARE = 0x16; // COM_STMT_PREPARE
    int PREPARED_STATEMENT_EXECUTE = 0x17; // COM_STMT_EXECUTE
    int PREPARED_STATEMENT_SEND_LONG_DATA = 0x18; // COM_STMT_SEND_LONG_DATA
    int AUTH_SWITCH_RESPONSE = 0xfe; // AuthSwitchRequest

    void encodeInto(ByteBuf packet);
    int packetSequenceNumber();
}
