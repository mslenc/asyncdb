package com.github.mslenc.asyncdb.util;

import javax.net.ssl.SSLContext;
import java.util.function.Supplier;

public class DefaultSslContextProvider implements Supplier<SSLContext> {
    @Override
    public SSLContext get() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, null, null);
            return ctx;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final DefaultSslContextProvider INSTANCE = new DefaultSslContextProvider();
}
