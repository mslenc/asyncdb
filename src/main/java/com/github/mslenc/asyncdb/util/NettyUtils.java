package com.github.mslenc.asyncdb.util;

import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class NettyUtils {
    private static final AtomicReference<NioEventLoopGroup> defaultGroupRef = new AtomicReference<>();

    public static NioEventLoopGroup getDefaultEventLoopGroup() {
        NioEventLoopGroup existing = defaultGroupRef.get();
        if (existing != null && !existing.isShuttingDown())
            return existing;

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("asyncdb-" + counter.getAndIncrement());
                thread.setDaemon(false);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };

        NioEventLoopGroup newGroup = new NioEventLoopGroup(0, threadFactory);

        if (defaultGroupRef.compareAndSet(existing, newGroup)) {
            return newGroup;
        } else {
            newGroup.shutdownGracefully();
            return getDefaultEventLoopGroup();
        }
    }
}
