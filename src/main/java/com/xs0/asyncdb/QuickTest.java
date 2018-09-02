package com.xs0.asyncdb;

import com.mysql.jdbc.Driver;
import com.xs0.asyncdb.common.Configuration;
import com.xs0.asyncdb.common.QueryResult;
import com.xs0.asyncdb.mysql.MySQLConnection;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class QuickTest {
    private static final int NUM_QUERIES = 50000;

    public static void main2(String[] args) throws Exception {
        DriverManager.registerDriver(new Driver());

        long begin = System.currentTimeMillis();

        try (java.sql.Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/eteam?user=eteam&password=qweqwe123123")) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT ? + ?")) {
                for (int a = 0; a < NUM_QUERIES; a++) {
                    ps.setInt(1, a);
                    ps.setInt(2, a + 1);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        System.out.println(rs.getInt(1));
                    }
                }
            }
        }

        long ended = System.currentTimeMillis();
        System.out.println(NUM_QUERIES + " queries in " + (ended - begin) + " ms");
    }

    public static void main(String[] args) throws InterruptedException {
        Configuration conf = new Configuration(
            "eteam",
            "127.0.0.1",
            3306,
            "qweqwe123123",
            "eteam",
            null,
            StandardCharsets.UTF_8,
            null,
            null,
            null,
            null,
            null
        );

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> overallError = new AtomicReference<>();

        MySQLConnection connection = new MySQLConnection(conf, null, null);

        long started = System.currentTimeMillis();

        connection.connect().whenComplete((conn, connError) -> {
            System.out.println("We have connection");

            if (connError != null) {
                overallError.set(connError);
                latch.countDown();
                return;
            }

            conn.prepareStatement("SELECT ? + ?").whenComplete((ps, psError) -> {
                System.out.println("We have prepared statement");

                if (psError != null) {
                    overallError.set(psError);
                    latch.countDown();
                    return;
                }

                ArrayList<CompletableFuture<QueryResult>> futures = new ArrayList<>();
                for (int i = 0; i < NUM_QUERIES; i++) {
                    System.out.println("Adding query " + i);
                    futures.add(ps.execute(Arrays.asList(i, i + 1)));
                }

                AtomicInteger remain = new AtomicInteger(futures.size());

                for (CompletableFuture<QueryResult> future : futures) {
                    future.whenComplete((queryResult, queryError) -> {
                        if (queryError != null) {
                            overallError.set(queryError);
                            latch.countDown();
                        } else {
                            System.out.println(queryResult.resultSet().get(0).get(0));

                            if (remain.decrementAndGet() <= 0) {
                                latch.countDown();
                            }
                        }
                    });
                }
            });
        });

        latch.await();
        if (overallError.get() != null)
            overallError.get().printStackTrace();

        long ended = System.currentTimeMillis();
        System.out.println(NUM_QUERIES + " queries in " + (ended - started) + " ms");
    }
}
