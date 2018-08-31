package com.xs0.asyncdb.common.util;

import java.util.concurrent.CompletableFuture;

public class FutureUtils {
    public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(throwable);
        return result;
    }
}
