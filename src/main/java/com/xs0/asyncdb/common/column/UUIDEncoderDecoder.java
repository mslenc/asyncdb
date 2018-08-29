package com.xs0.asyncdb.common.column;

import java.util.UUID;

public class UUIDEncoderDecoder implements ColumnEncoderDecoder {
    private static final UUIDEncoderDecoder instance = new UUIDEncoderDecoder();

    public static UUIDEncoderDecoder instance() {
        return instance;
    }

    @Override
    public UUID decode(String value) {
        return UUID.fromString(value);
    }
}
