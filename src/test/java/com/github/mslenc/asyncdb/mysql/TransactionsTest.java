package com.github.mslenc.asyncdb.mysql;

import org.junit.Test;

public class TransactionsTest {
    static Object[][] singleVal(Object value) {
        return new Object[][] { { value } };
    }

    @Test
    public void testBasicTransactionSupport() {
        String INSERT = "INSERT INTO tx_table(id, value) VALUES(?, ?)";
        String SELECT_MAX = "SELECT MAX(id) FROM tx_table";

        TestHelper.runTest((conn, helper, testFinished) -> {
            helper.expectSuccess(conn.execute("DROP TABLE IF EXISTS tx_table"));
            helper.expectSuccess(conn.execute("CREATE TABLE tx_table(id INT NOT NULL, value VARCHAR(255) NOT NULL)"));
            helper.expectSuccess(conn.executeUpdate(INSERT,1, "abc"), (ignored1) -> {
                helper.expectSuccess(TestHelper.db.connect(), (conn2) -> {
                    helper.expectSuccess(conn2.executeUpdate(INSERT, 2, "def"), (ignored2) -> {
                        helper.expectResultSetValues(conn.executeQuery(SELECT_MAX), singleVal(2), (ignored3) -> {
                            helper.expectSuccess(conn2.startTransaction());
                            helper.expectSuccess(conn2.executeUpdate(INSERT, 3, "qwe"), (ignored4) -> {
                                helper.expectResultSetValues(conn.executeQuery(SELECT_MAX), singleVal(2), (ignored5) -> {
                                    helper.expectSuccess(conn2.commitAndChain());
                                    helper.expectSuccess(conn2.executeUpdate(INSERT, 4, "qsa"), (ignored6) -> {
                                        helper.expectResultSetValues(conn.executeQuery(SELECT_MAX), singleVal(3), (ignored7) -> {
                                            helper.expectSuccess(conn2.close(), (ignored8) -> {
                                                helper.expectSuccess(TestHelper.db.connect(), (conn3) -> {
                                                    helper.expectSuccess(conn3.startTransaction(), (ignored9) -> {
                                                        helper.expectResultSetValues(conn.executeQuery(SELECT_MAX), singleVal(3), (ignored10) -> {
                                                            helper.expectSuccess(conn.close());
                                                            helper.expectSuccess(conn3.close(), (ignored11) -> {
                                                                testFinished.complete(null);
                                                            });
                                                        });
                                                    });
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }
}
