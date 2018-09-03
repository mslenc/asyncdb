package com.xs0.asyncdb.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadsFactory implements ThreadFactory {
    private final String threadNamePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public DaemonThreadsFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = Executors.defaultThreadFactory().newThread(r);

        thread.setDaemon(true);
        thread.setName(threadNamePrefix + "-thread-" + threadNumber.getAndIncrement());

        return thread;
    }
}
