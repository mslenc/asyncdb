package com.xs0.asyncdb.common.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorServiceUtils {
    public static final Executor cachedThreadPool = Executors.newCachedThreadPool(new DaemonThreadsFactory("asyncdb-default"));
}
