package com.github.mslenc.asyncdb;

public interface DbResultObserver {
    /**
     * Called in turn for each row in the query result.
     */
    void onNext(DbRow row);

    /**
     * Called if an error occurs during query evaluation. This will be the last call.
     */
    void onError(Throwable t);

    /**
     * Called after the query completes successfully (with null DbResultSet within queryResult). This will be the last call.
     */
    void onCompleted();
}
