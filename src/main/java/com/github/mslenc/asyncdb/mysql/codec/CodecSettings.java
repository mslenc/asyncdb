package com.github.mslenc.asyncdb.mysql.codec;

import java.nio.charset.Charset;
import java.time.ZoneId;

public class CodecSettings {
    private final ZoneId timezone;
    private final Charset charset;

    public CodecSettings(Charset charset, ZoneId timezone) {
        this.timezone = timezone;
        this.charset = charset;
    }

    public ZoneId timezone() {
        return timezone;
    }

    public Charset charset() {
        return charset;
    }
}
