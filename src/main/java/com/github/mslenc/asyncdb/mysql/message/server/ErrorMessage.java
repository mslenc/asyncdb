package com.github.mslenc.asyncdb.mysql.message.server;

public class ErrorMessage implements ServerMessage {
    public final int errorCode;
    public final String sqlState;
    public final String errorMessage;

    public ErrorMessage(int errorCode, String sqlState, String errorMessage) {
        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
    }
}