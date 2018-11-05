package com.github.mslenc.asyncdb.util;

import com.github.mslenc.asyncdb.DbExecResult;
import com.github.mslenc.asyncdb.DbResultSet;
import com.github.mslenc.asyncdb.DbUpdateResult;

import java.util.concurrent.CompletableFuture;

public class MiscUtils {
    public static CompletableFuture<DbResultSet> extractResultSet(CompletableFuture<DbExecResult> future) {
        CompletableFuture<DbResultSet> promise = new CompletableFuture<>();

        future.whenComplete((queryResult, error) -> {
            if (error != null) {
                promise.completeExceptionally(error);
                return;
            }

            DbResultSet resultSet = queryResult.getResultSet();
            if (resultSet == null) {
                promise.completeExceptionally(new IllegalStateException("There was no result set"));
                return;
            }

            promise.complete(resultSet);
        });

        return promise;
    }

    public static CompletableFuture<DbUpdateResult> extractUpdateResult(CompletableFuture<DbExecResult> future) {
        CompletableFuture<DbUpdateResult> promise = new CompletableFuture<>();

        future.whenComplete((queryResult, error) -> {
            if (error != null) {
                promise.completeExceptionally(error);
                return;
            }

            promise.complete(queryResult);
        });

        return promise;
    }

    public static CompletableFuture<Void> extractVoid(CompletableFuture<DbExecResult> future) {
        CompletableFuture<Void> promise = new CompletableFuture<>();

        future.whenComplete((queryResult, error) -> {
            if (error != null) {
                promise.completeExceptionally(error);
                return;
            }

            promise.complete(null);
        });

        return promise;
    }
}
