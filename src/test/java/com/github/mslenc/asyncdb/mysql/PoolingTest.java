package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.DbConnection;
import com.github.mslenc.asyncdb.DbRow;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PoolingTest {
    static final int N_DATABASES = 8;
    static final int N_RUNS = 128;

    final AtomicInteger successes = new AtomicInteger(0);
    final AtomicInteger failures = new AtomicInteger(0);

    @Before
    public void initCounters() {
        successes.set(0);
        failures.set(0);
    }

    @Test
    public void testWithDifferentCredentials() {
        // We will make a bunch of databases, with corresponding users; we will also "forget" to make some of them,
        // to test behavior with login errors. In addition, we make two databases per user, to see if we succeed
        // in just switching the default database without re-authenticating (INIT_DB vs CHANGE_USER).
        // Then, we will randomly connect to a bunch of them, and count successes and failures.

        TestHelper.runTest(300000, "root", "testpassword",
            (conn, helper, testFinished) ->
                helper.expectSuccess(makeTables(conn, helper),
            tables ->
                helper.expectSuccess(makeUsers(conn, helper, tables),
            users ->
                runUsersTests(tables, users, helper).whenComplete(
                    (result, error) -> {
                        if (error != null) {
                            testFinished.completeExceptionally(error);
                        } else {
                            int expectSuccesses = N_RUNS * users.size();
                            int expectFailures = N_RUNS * (N_DATABASES - users.size());

                            if (successes.get() == expectSuccesses && failures.get() == expectFailures) {
                                testFinished.complete(null);
                            } else {
                                testFinished.completeExceptionally(new AssertionError("Count is wrong - successes: " + successes.get() + " vs " + expectSuccesses + ", failures: " + failures.get() + " vs " + expectFailures));
                            }
                        }
                    }
        ))));
    }

    private CompletableFuture<?> runUsersTests(Set<Integer> tableIds, Set<Integer> userIds, TestHelper helper) {
        ArrayList<CompletableFuture<?>> promises = new ArrayList<>();

        for (int a = 0; a < N_RUNS; a++) {
            for (int id = 0; id < N_DATABASES; id++) {
                promises.add(runUserTest(id, userIds.contains(id), helper));
            }
        }

        return CompletableFuture.allOf(promises.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<?> runUserTest(int id, boolean expectSuccess, TestHelper helper) {
        String userName = "user" + id;
        String userPass = "pass" + id;
        String dbName = (ThreadLocalRandom.current().nextBoolean() ? "testDb" : "testDbb") + id;
        String tableName = "testTable" + id;

        CompletableFuture<Object> promise = new CompletableFuture<>();

        if (expectSuccess) {
            helper.expectSuccess(TestHelper.db.connect(userName, userPass, dbName), conn -> {
                helper.expectSuccess(conn.executeQuery("SELECT USER(), CURRENT_USER(), DATABASE(), COUNT(*) FROM " + tableName), resultSet -> {
                    try {
                        assertEquals(1, resultSet.size());
                        DbRow row = resultSet.get(0);
                        assertTrue(row.getString(0).startsWith(userName + "@"));
                        assertTrue(row.getString(1).startsWith(userName + "@"));
                        assertEquals(dbName, row.getString(2));
                        assertEquals(0, row.getInt(3));
                        successes.incrementAndGet();
                    } finally {
                        conn.close();
                        promise.complete(null);
                    }
                });
            });
        } else {
            helper.expectFailure(TestHelper.db.connect(userName, userPass, dbName), error -> {
                failures.incrementAndGet();
                promise.complete(null);
            });
        }

        return promise;
    }

    CompletableFuture<Set<Integer>> makeTables(DbConnection conn, TestHelper helper) {
        ArrayList<CompletableFuture<?>> promises = new ArrayList<>();
        Set<Integer> tableIds = new LinkedHashSet<>();

        for (int a = 0; a < N_DATABASES; a++) {
            promises.add(conn.execute("DROP DATABASE IF EXISTS testDb" + a));
            promises.add(conn.execute("DROP DATABASE IF EXISTS testDbb" + a));
        }

        while (tableIds.isEmpty()) {
            for (int a = 0; a < N_DATABASES; a++) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    tableIds.add(a);
                    String dbName1 = "testDb" + a;
                    String dbName2 = "testDbb" + a;
                    String tableName = "testTable" + a;

                    promises.add(conn.execute("CREATE DATABASE " + dbName1));
                    promises.add(conn.execute("CREATE DATABASE " + dbName2));
                    promises.add(conn.execute("CREATE TABLE " + dbName1 + "." + tableName + "(id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))"));
                    promises.add(conn.execute("CREATE TABLE " + dbName2 + "." + tableName + "(id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))"));
                }
            }
        }
        promises.forEach(helper::expectSuccess);

        CompletableFuture<Set<Integer>> promise = new CompletableFuture<>();

        CompletableFuture.allOf(promises.toArray(new CompletableFuture[0])).whenComplete((result, error) -> {
            if (error != null) {
                promise.completeExceptionally(error);
            } else {
                promise.complete(tableIds);
            }
        });

        return promise;
    }

    CompletableFuture<Set<Integer>> makeUsers(DbConnection conn, TestHelper helper, Set<Integer> tableIds) {
        ArrayList<CompletableFuture<?>> promises = new ArrayList<>();

        for (int id = 0; id < N_DATABASES; id++) {
            String userName = "user" + id;
            String userPass = "pass" + id;
            String dbName1 = "testDb" + id;
            String dbName2 = "testDbb" + id;
            String tableName = "testTable" + id;

            String user = "'" + userName + "'@'%'";

            promises.add(conn.execute("DROP USER IF EXISTS " + user));
            if (tableIds.contains(id)) {
                promises.add(conn.execute("CREATE USER " + user + " IDENTIFIED BY '" + userPass + "'"));
                promises.add(conn.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON " + dbName1 + "." + tableName + " TO " + user));
                promises.add(conn.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON " + dbName2 + "." + tableName + " TO " + user));
            }
        }
        promises.forEach(helper::expectSuccess);

        CompletableFuture<Set<Integer>> promise = new CompletableFuture<>();

        CompletableFuture.allOf(promises.toArray(new CompletableFuture[0])).whenComplete((result, error) -> {
            if (error != null) {
                promise.completeExceptionally(error);
            } else {
                promise.complete(tableIds);
            }
        });

        return promise;
    }
}
