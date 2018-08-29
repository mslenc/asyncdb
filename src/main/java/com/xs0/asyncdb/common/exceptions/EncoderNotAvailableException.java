package com.xs0.asyncdb.common.exceptions;

import com.xs0.asyncdb.common.KindedMessage;

public class EncoderNotAvailableException extends DatabaseException {
    public EncoderNotAvailableException(KindedMessage message) {
        super("Encoder not available for message type " + message.kind());
    }
}
