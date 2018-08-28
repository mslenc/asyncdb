package com.xs0.asyncdb.mysql.message.server;

import com.xs0.asyncdb.common.KindedMessage;

public interface ServerMessage extends KindedMessage {
    int ServerProtocolVersion = 10;
    int Error = -1;
    int Ok = 0;
    int EOF = -2;

    // these messages don't actually exist
    // but we use them to simplify the switch statements
    int ColumnDefinition = 100;
    int ColumnDefinitionFinished = 101;
    int ParamProcessingFinished = 102;
    int ParamAndColumnProcessingFinished = 103;
    int Row = 104;
    int BinaryRow = 105;
    int PreparedStatementPrepareResponse = 106;
}
