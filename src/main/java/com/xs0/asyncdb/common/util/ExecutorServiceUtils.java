package com.xs0.asyncdb.common.util;

import com.xs0.asyncdb.common.ExecutionContext;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(ExecutorServiceUtils.class);

    public static final Executor cachedThreadPool = Executors.newCachedThreadPool(new DaemonThreadsFactory("asyncdb-default"));

    public static ExecutionContext wrap(Executor executor) {
        return ExecutionContext.createFrom(executor, error -> log.error("An error was reported", error));
    }
}
