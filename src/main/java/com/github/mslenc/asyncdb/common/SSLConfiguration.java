package com.github.mslenc.asyncdb.common;

import java.nio.file.Path;

public class SSLConfiguration {
    enum Mode {
        DISABLE("disable"),
        PREFER("prefer"),
        REQUIRE("require"),
        VERIFY_CA("verify-ca"),
        VERIFY_FULL("verify-full");

        private final String asString;

        Mode(String asString) {
            this.asString = asString;
        }
    }

    private final Mode mode;
    private final Path rootCertFile;

    public SSLConfiguration(Mode mode) {
        this(mode, null);
    }

    public SSLConfiguration(Mode mode, Path rootCertFile) {
        this.mode = mode;
        this.rootCertFile = rootCertFile;
    }

    public Mode mode() {
        return mode;
    }

    public Path rootCertFile() {
        return rootCertFile;
    }
}
