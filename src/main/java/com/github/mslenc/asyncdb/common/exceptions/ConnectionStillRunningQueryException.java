package com.github.mslenc.asyncdb.common.exceptions;

public class ConnectionStillRunningQueryException extends DatabaseException {
    public ConnectionStillRunningQueryException(long connectionCount, boolean caughtRace) {
        super("[" + connectionCount + "] - A query is still running - race -> " + caughtRace);
    }
}
