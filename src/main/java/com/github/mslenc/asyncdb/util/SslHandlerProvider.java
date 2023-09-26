package com.github.mslenc.asyncdb.util;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslHandler;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface SslHandlerProvider {
    SslHandler create(ByteBufAllocator alloc) throws IOException, GeneralSecurityException;
}
