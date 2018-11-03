package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.DbConfig;
import com.github.mslenc.asyncdb.DbDataSource;
import com.github.mslenc.asyncdb.ex.ConnectionTimeoutException;
import io.netty.channel.ConnectTimeoutException;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TimeoutTest {
    @Test
    public void testConnectTimeout() throws ExecutionException, InterruptedException {
        DbConfig config =
            TestHelper.config.toBuilder().
                setConnectTimeout(Duration.ofMillis(10)).
                setHost("10.255.255.1").
            build();

        CompletableFuture<Void> result = new CompletableFuture<>();

        DbDataSource dataSource = config.makeDataSource();

        dataSource.connect().whenComplete((conn, error) -> {
            if (error instanceof ConnectTimeoutException) {
                result.complete(null);
            } else
            if (error != null) {
                // note that if there is a system-based timeout, we will not get a ConnectTimeoutException,
                // however, with the short duration in this test, it's not expected to happen..
                result.completeExceptionally(new AssertionError("Exception was not ConnectTimeoutException", error));
            } else {
                result.completeExceptionally(new AssertionError("Did not timeout"));
            }
        });

        result.get();
    }

    private static final String SHORT_QUERY = "SELECT DISTINCT TABLE_CATALOG FROM information_schema.COLUMNS";
    private static final String LONG_QUERY = "SELECT sha2(concat(a.TABLE_CATALOG, b.TABLE_CATALOG, c.TABLE_CATALOG), 512) AS bubu " +
                                             "FROM information_Schema.COLUMNS a, information_Schema.COLUMNS b, information_Schema.COLUMNS c " +
                                             "HAVING bubu='abc'"; // <-- this is just to avoid transferring zillions of bytes of useless data..

    @Test
    public void testQueryTimeout() throws ExecutionException, InterruptedException {
        DbConfig config =
            TestHelper.config.toBuilder().
                setQueryTimeout(Duration.ofMillis(500)).
            build();

        CompletableFuture<Void> result = new CompletableFuture<>();

        DbDataSource dataSource = config.makeDataSource();

        dataSource.connect().whenComplete((conn, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }

            // first we try a tiny query which should complete well within 500 millis
            conn.sendQuery(SHORT_QUERY).whenComplete((query1res, error1) -> {
                if (error1 != null) {
                    result.completeExceptionally(error1);
                    return;
                }

                // and then we try a really long running query, and expect to get a timeout
                conn.sendQuery(LONG_QUERY).whenComplete((query2res, error2) -> {
                    try {
                        if (error2 instanceof ConnectionTimeoutException) {
                            result.complete(null);
                            return;
                        }

                        if (error2 != null) {
                            result.completeExceptionally(new AssertionError("The exception was not a ConnectionTimeoutException"));
                        } else {
                            result.completeExceptionally(new AssertionError("The query did not time out"));
                        }
                    } finally {
                        conn.close();
                    }
                });
            });
        });

        result.get();
    }
}
