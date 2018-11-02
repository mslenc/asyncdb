package com.github.mslenc.asyncdb.impl.values;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DbValueByteArray extends AbstractDbValue {
    private final byte[] bytes;

    public DbValueByteArray(byte[] bytes) {
        this.bytes = Objects.requireNonNull(bytes);
    }

    @Override
    protected String typeName() {
        return "byte[]";
    }

    @Override
    public byte[] unwrap() {
        return bytes;
    }

    @Override
    public boolean asBoolean() {
        return bytes.length > 0;
    }

    @Override
    public byte[] asByteArray() {
        return bytes;
    }

    @Override
    public byte asByte() {
        if (bytes.length == 1) {
            return bytes[0];
        } else {
            return super.asByte();
        }
    }

    @Override
    public String asString() {
        // TODO: TEST: is this a good string representation for going back to db?
        if (bytes.length > 0) {
            return new String(bytes, UTF_8);
        } else {
            return "";
        }
    }
}
