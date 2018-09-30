package com.github.mslenc.asyncdb.mysql.codec;

import java.time.ZoneId;

public class CodecSettings {
    private final ZoneId localTimezone;
    private final ZoneId serverTimezone;

    public CodecSettings(ZoneId localTimezone, ZoneId serverTimezone) {
        this.localTimezone = localTimezone;
        this.serverTimezone = serverTimezone;
    }

    public ZoneId localTimezone() {
        return localTimezone;
    }

    public ZoneId serverTimezone() {
        return serverTimezone;
    }
}
