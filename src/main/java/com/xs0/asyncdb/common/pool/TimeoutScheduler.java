package com.xs0.asyncdb.common.pool;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TimeoutScheduler {
    private AtomicBoolean isTimeoutedBool = new AtomicBoolean(false);

    /**
     * The event loop group to be used for scheduling.
     */
    protected abstract EventLoopGroup eventLoopGroup();

    /**
     * Implementors should decide here what they want to do when a timeout occur
     */
    protected abstract void onTimeout(); // implementors should decide here what they want to do when a timeout occur

    /**
     *
     * We need this property as isClosed takes time to complete and
     * we don't want the connection to be used again.
     *
     * @return
     */

    protected boolean isTimeouted() {
        return isTimeoutedBool.get();
    }

    protected ScheduledFuture<?> schedule(Duration duration, Runnable block) {
        return eventLoopGroup().schedule(block, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> addTimeout(CompletableFuture<?> promise, Duration duration) {
        if (duration == null)
            return null;

        ScheduledFuture<?> scheduledFuture = schedule(duration, () -> {
            if (promise.completeExceptionally(new TimeoutException("Operation timed out after it took too long to return (" + duration + ")"))) {
                isTimeoutedBool.set(true);
                onTimeout();
            }
        });

        promise.thenRun(() -> scheduledFuture.cancel(false));

        return scheduledFuture;
    }
}
