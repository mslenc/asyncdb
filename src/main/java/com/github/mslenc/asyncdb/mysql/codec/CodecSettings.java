package com.github.mslenc.asyncdb.mysql.codec;

import java.nio.charset.Charset;
import java.time.ZoneId;

public class CodecSettings {
    private final ZoneId localTimezone;
    private final ZoneId serverTimezone;
    private final Charset charset;

    public CodecSettings(Charset charset, ZoneId localTimezone, ZoneId serverTimezone) {
        this.charset = charset;
        this.localTimezone = localTimezone;
        this.serverTimezone = serverTimezone;
    }

    public ZoneId localTimezone() {
        return localTimezone;
    }

    public ZoneId serverTimezone() {
        return serverTimezone;
    }

    public Charset charset() {
        return charset;
    }
}
