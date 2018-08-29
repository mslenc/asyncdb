package com.xs0.asyncdb.mysql.message.server;

import com.xs0.asyncdb.common.KindedMessage;

public interface ServerMessage extends KindedMessage {
    int SERVER_PROTOCOL_VERSION = 10;
    int ERROR = -1;
    int OK = 0;
    int EOF = -2;

    // these messages don't actually exist
    // but we use them to simplify the switch statements
    int COLUMN_DEFINITION = 100;
    int COLUMN_DEFINITION_FINISHED = 101;
    int PARAM_PROCESSING_FINISHED = 102;
    int PARAM_AND_COLUMN_PROCESSING_FINISHED = 103;
    int ROW = 104;
    int BINARY_ROW = 105;
    int PREPARED_STATEMENT_PREPARE_RESPONSE = 106;
}
