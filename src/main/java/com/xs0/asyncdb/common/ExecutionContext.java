package com.xs0.asyncdb.common;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface ExecutionContext extends Executor {
    void reportFailure(Throwable cause);

    static ExecutionContext createFrom(Executor executor, Consumer<Throwable> failureConsumer) {
        return new ExecutionContext() {
            @Override
            public void reportFailure(Throwable cause) {
                if (failureConsumer != null)
                    failureConsumer.accept(cause);
            }

            @Override
            public void execute(@NotNull Runnable command) {
                executor.execute(command);
            }
        };
    }
}
