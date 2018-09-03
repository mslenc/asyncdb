package com.xs0.asyncdb.common.util;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

public class NettyUtils {
    static {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    }

    public static final NioEventLoopGroup defaultEventLoopGroup = new NioEventLoopGroup(0, new DaemonThreadsFactory("asyncdb-netty"));
}
