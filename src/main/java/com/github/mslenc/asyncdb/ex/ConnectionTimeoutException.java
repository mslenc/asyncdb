package com.github.mslenc.asyncdb.ex;

import java.time.Duration;

public class ConnectionTimeoutException extends ConnectionClosedException {
    public ConnectionTimeoutException(Duration duration) {
        super("An operation timed out after " + duration);
    }
}
