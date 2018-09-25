package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.common.Configuration;
import com.github.mslenc.asyncdb.common.Connection;
import com.github.mslenc.asyncdb.common.QueryResult;
import com.github.mslenc.asyncdb.common.ResultSet;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class TestHelper {
    private ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
    private AtomicInteger futureCounter = new AtomicInteger(0);
    private LinkedBlockingDeque<Integer> backQueue = new LinkedBlockingDeque<>();

    interface TestContents {
        void start(Connection conn, TestHelper helper, CompletableFuture<Void> testFinished);
    }


    static Configuration config(boolean rootUser, String database) {
        return Configuration.newMySQLBuilder().
            setPort(3356).
            setUsername(rootUser ? "root" : "asyncdb").
            setPassword(rootUser ? "testpassword": "asyncdb").
            setDatabase(database).
            build();
    }

    public static void runTest(TestContents test) {
        runTest(10000L, test);
    }

    private static Integer pollWithDeadline(BlockingDeque<Integer> queue, long deadline) throws InterruptedException {
        long remain = deadline - System.currentTimeMillis();
        if (remain < 0)
            return null;

        return queue.pollFirst(remain, TimeUnit.MILLISECONDS);
    }

    public static void runTest(long timeoutMillis, TestContents test) {
        Configuration config = config(false, "asyncdb");

        assertTrue(timeoutMillis > 0 && timeoutMillis <= 300000);

        TestHelper testHelper = new TestHelper();

        long deadline = System.currentTimeMillis() + timeoutMillis;

        MySQLConnection connection = new MySQLConnection(config, null);
        connection.connect().whenCompleteAsync((conn, error) -> {
            if (error != null) {
                testHelper.errors.add(error);
                testHelper.futureCounter.incrementAndGet();
                testHelper.backQueue.add(-1);
                return;
            }

            try {
                CompletableFuture<Void> testFinished = new CompletableFuture<>();
                testHelper.expectSuccess(testFinished);

                test.start(conn, testHelper, testFinished);
            } catch (Throwable t) {
                testHelper.errors.add(t);
                testHelper.backQueue.add(-2);
                return;
            }

            // and off we go...
        });

        int messagesReceived = 0;
        do {
            Integer message;
            try {
                message = pollWithDeadline(testHelper.backQueue, deadline);
            } catch (InterruptedException e) {
                throw new AssertionError("Did not expect to be interrupted", e);
            }

            assertNotNull("The test timed out after " + timeoutMillis + " ms", message);

            if (message < 0) {
                Throwable error = testHelper.errors.poll();
                if (error == null) {
                    fail("An unknown error occurred");
                } else {
                    throw new AssertionError(error);
                }
            } else {
                messagesReceived++;
            }
        } while (messagesReceived < testHelper.futureCounter.get());

        if (testHelper.errors.isEmpty())
            return; // yay :)

        throw new AssertionError(testHelper.errors.poll());
    }

    private void futureFinished(CompletableFuture<?> future) {
        backQueue.add(0);
    }

    private <T> CompletableFuture<T> futureStarting(CompletableFuture<T> future) {
        futureCounter.incrementAndGet();
        return future;
    }

    public void expectSuccess(CompletableFuture<?> future) {
        futureStarting(future).whenCompleteAsync((result, error) -> {
            try {
                if (error != null) {
                    error.printStackTrace();
                    errors.add(error);
                }
            } finally {
                futureFinished(future);
            }
        });
    }

    public void expectFailure(CompletableFuture<?> future) {
        Exception potentialFailure = new Exception("Expected failure");
        potentialFailure.fillInStackTrace();

        futureStarting(future).whenCompleteAsync((result, error) -> {
            try {
                if (error == null) {
                    errors.add(potentialFailure);
                }
            } finally {
                futureFinished(future);
            }
        });
    }

    public <T> void expectSuccess(CompletableFuture<T> future, Consumer<T> onResult) {
        futureStarting(future).whenCompleteAsync((result, error) -> {
            try {
                if (error != null) {
                    error.printStackTrace();
                    errors.add(error);
                } else {
                    try {
                        onResult.accept(result);
                    } catch (Throwable t) {
                        errors.add(t);
                    }
                }

            } finally {
                futureFinished(future);
            }
        });
    }

    public void expectFailure(CompletableFuture<?> future, Consumer<Throwable> onError) {
        Exception potentialFailure = new Exception("Expected failure");
        potentialFailure.fillInStackTrace();

        futureStarting(future).whenCompleteAsync((result, error) -> {
            try {
                if (error == null) {
                    errors.add(potentialFailure);
                } else {
                    try {
                        onError.accept(error);
                    } catch (Throwable t) {
                        errors.add(t);
                    }
                }
            } finally {
                futureFinished(future);
            }
        });
    }

    public void expectResultSet(CompletableFuture<QueryResult> future) {
        expectSuccess(future, queryResult -> assertNotNull(queryResult.resultSet()));
    }

    public void expectResultSet(CompletableFuture<QueryResult> future, Consumer<ResultSet> onResult) {
        expectSuccess(future, queryResult -> {
            ResultSet resultSet = queryResult.resultSet();
            assertNotNull(resultSet);
            onResult.accept(resultSet);
        });
    }

    public void expectResultSetValues(CompletableFuture<QueryResult> sendQuery, Object[][] rows) {
        expectResultSet(sendQuery, resultSet -> {
            assertEquals(rows.length, resultSet.size());

            int numCols = resultSet.getColumnNames().size();

            for (int r = 0; r < rows.length; r++) {
                assertEquals(rows[r].length, numCols);

                for (int c = 0; c < numCols; c++) {
                    Object expected = rows[r][c];
                    Object received = resultSet.get(r).get(c);

                    if (expected == null) {
                        assertNull(received);
                        continue;
                    }

                    if (expected.getClass().isArray()) {
                        assertTrue(Arrays.deepEquals(new Object[] { expected }, new Object[] { received }));
                    } else {
                        assertEquals(expected, received);
                    }
                }
            }
        });
    }
}
