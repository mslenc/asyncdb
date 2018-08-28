package com.xs0.asyncdb.mysql.message.client;

import com.xs0.asyncdb.common.KindedMessage;

public interface ClientMessage extends KindedMessage {
    int ClientProtocolVersion = 0x09; // COM_STATISTICS
    int Quit = 0x01; // COM_QUIT
    int Query = 0x03; // COM_QUERY
    int PreparedStatementPrepare = 0x16; // COM_STMT_PREPARE
    int PreparedStatementExecute = 0x17; // COM_STMT_EXECUTE
    int PreparedStatementSendLongData = 0x18; // COM_STMT_SEND_LONG_DATA
    int AuthSwitchResponse = 0xfe; // AuthSwitchRequest
}
