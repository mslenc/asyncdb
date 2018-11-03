package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.DbQueryResultObserver;
import com.github.mslenc.asyncdb.DbResultSet;
import com.github.mslenc.asyncdb.DbRow;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StreamingTest {
    static void assertRowsEqual(DbRow expect, DbRow actual) {
        assertColumnsEqual(expect.getColumns(), actual.getColumns());

        int n = expect.getColumns().size();
        for (int a = 0; a < n; a++) {
            assertEquals(expect.getValue(a), actual.getValue(a));
        }
    }

    static void assertColumnsEqual(DbColumns expect, DbColumns actual) {
        assertEquals(expect.size(), actual.size());

        int n = expect.size();
        for (int a = 0; a < n; a++)
            assertEquals(expect.get(a), actual.get(a));
    }

    @Test
    public void testStreamingRowsEqualNormalRows() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            Random rnd = new Random();

            HashSet<Integer> numsSet = new HashSet<>();
            for (int a = 0; a < 27; a++)
                numsSet.add(rnd.nextInt());

            Integer[] nums = numsSet.toArray(new Integer[0]);
            Arrays.sort(nums);
            int last = nums.length - 1;

            helper.expectSuccess(conn.execute("DROP TABLE IF EXISTS some_nums"), ignored -> {
                helper.expectSuccess(conn.execute("CREATE TABLE some_nums(id INT NOT NULL PRIMARY KEY, as_text VARCHAR(50) NOT NULL)"), ignored2 -> {
                    for (int a = 0; a < last; a++)
                        helper.expectSuccess(conn.execute("INSERT INTO some_nums(id, as_text) VALUES(?, ?)", nums[a], "bubu" + nums[a]));

                    helper.expectSuccess(conn.execute("INSERT INTO some_nums(id, as_text) VALUES(?, ?)", nums[last], "bubu" + nums[last]), ignored3 -> {
                        helper.expectSuccess(conn.execute("SELECT id, as_text FROM some_nums ORDER BY id"), queryResult -> {
                            DbResultSet expect = queryResult.getResultSet();
                            assertNotNull(expect);

                            conn.streamQuery("SELECT id, as_text FROM some_nums ORDER BY id", new DbQueryResultObserver() {
                                int rowCount = 0;

                                @Override
                                public void onNext(DbRow row) {
                                    assertTrue(rowCount <= expect.size());
                                    DbRow expectRow = expect.get(rowCount++);

                                    assertRowsEqual(expectRow, row);
                                }

                                @Override
                                public void onError(Throwable t) {
                                    testFinished.completeExceptionally(t);
                                }

                                @Override
                                public void onCompleted() {
                                    assertEquals(expect.size(), rowCount);
                                    testFinished.complete(null);
                                }
                            });
                        });
                    });
                });
            });
        });
    }
}
