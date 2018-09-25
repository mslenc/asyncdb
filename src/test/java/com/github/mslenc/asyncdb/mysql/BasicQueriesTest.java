package com.github.mslenc.asyncdb.mysql;

import org.junit.Test;

import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicQueriesTest {
    @Test
    public void testMySQLCanDoArithmetic() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            Object[][] expect = {
                { 3L, new BigDecimal("3.5") }
            };

            helper.expectResultSetValues(conn.sendQuery("SELECT 1 + 2, 3.1 + 0.4"), expect);
            helper.expectSuccess(conn.disconnect());
            testFinished.complete(null);
        });
    }

    @Test
    public void testMySQLCanRememberStuff() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            helper.expectSuccess(conn.sendQuery(
                "DROP TABLE IF EXISTS first_table"
            ));

            helper.expectSuccess(conn.sendQuery(
                "CREATE TABLE first_table(" +
                    "id INT NOT NULL PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "a_number DECIMAL(20, 10) NOT NULL" +
                ")"
            ));

            helper.expectSuccess(conn.sendQuery("INSERT INTO first_table VALUES(?, ?, ?)", asList(1, "Name 1",         BigDecimal.ZERO)));
            helper.expectSuccess(conn.sendQuery("INSERT INTO first_table VALUES(?, ?, ?)", asList(2, "Another name 2", BigDecimal.ONE)));
            helper.expectSuccess(conn.sendQuery("INSERT INTO first_table VALUES(?, ?, ?)", asList(3, "Last name 3",    new BigDecimal("1234567890.0987654321"))));

            helper.expectResultSet(conn.sendQuery("SELECT id, name, a_number FROM first_table ORDER BY id"), resultSet -> {
                assertEquals(3, resultSet.size());

                assertEquals(1, resultSet.get(0).get(0));
                assertEquals(2, resultSet.get(1).get(0));
                assertEquals(3, resultSet.get(2).get(0));

                assertEquals("Name 1", resultSet.get(0).get(1));
                assertEquals("Another name 2", resultSet.get(1).get(1));
                assertEquals("Last name 3", resultSet.get(2).get(1));

                assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) resultSet.get(0).get(2)));
                assertEquals(0, BigDecimal.ONE.compareTo((BigDecimal) resultSet.get(1).get(2)));
                assertEquals(0, new BigDecimal("1234567890.0987654321").compareTo((BigDecimal) resultSet.get(2).get(2)));

                helper.expectSuccess(conn.disconnect());

                testFinished.complete(null);
            });
        });
    }
}
