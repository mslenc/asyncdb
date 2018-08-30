package com.xs0.asyncdb.common.util;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.concurrent.CompletableFuture;

public class NettyUtils {
    static {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    }

    public static final NioEventLoopGroup defaultEventLoopGroup = new NioEventLoopGroup(0, new DaemonThreadsFactory("asyncdb-netty"));

    public static <T> void forwardResult(Future<T> nettyFuture, CompletableFuture<T> completableFuture) {
        nettyFuture.addListener((Future<T> f) -> {
            if (f.isSuccess()) {
                completableFuture.complete(f.get());
            } else {
                completableFuture.completeExceptionally(f.cause());
            }
        });
    }

    public static <T> void forwardFailureOnly(Future<T> nettyFuture, CompletableFuture<T> completableFuture) {
        nettyFuture.addListener((Future<T> f) -> {
            if (!f.isSuccess()) {
                completableFuture.completeExceptionally(f.cause());
            }
        });
    }

    public static <T> CompletableFuture<T> toCompFuture(Future<T> future) {
        CompletableFuture<T> result = new CompletableFuture<>();
        forwardResult(future, result);
        return result;
    }
}
