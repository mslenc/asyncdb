package com.github.mslenc.asyncdb.mysql;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class EscapingQueriesTest {
    @Test
    public void testQueryValueEscaping() {
        TestHelper.runTest((conn, helper, testFinished) -> {
            String dropIfExistsSql =
                "DROP TABLE IF EXISTS `weird!\"tab?le\"name?`";

            String createSql =
                "CREATE TABLE `weird!\"tab?le\"name?` (" +
                    "    \"id?\" int not null primary key," +
                    "    `there?` tinyint not null," +
                    "    \"text1?\" text," +
                    "    `text2?` text" +
                ");";

            String insertSql =
                "INSERT INTO `weird!\"tab?le\"name?` SET " +
                    "\"id?\" = ?," +
                    "`there?` = ?," +
                    "\"text1?\" = ?," +
                    "`text2?` = 'true, or is it??'";

            String selectSql = "SELECT * from `weird!\"tab?le\"name?` WHERE `id?` < ? ORDER BY `id?`";

            String dropSql = "DROP TABLE `weird!\"tab?le\"name?`";

            Object[][] toSend = {
                { 1, true, null },
                { 2, false, "abc???" },
                { 3, true, "" }
            };

            Object[][] expect = {
                { 1, 1, null,     "true, or is it??" },
                { 2, 0, "abc???", "true, or is it??" },
                { 3, 1, "",       "true, or is it??" }
            };

            helper.expectSuccess(conn.sendQuery(dropIfExistsSql));
            helper.expectSuccess(conn.sendQuery(createSql));

            helper.expectSuccess(conn.sendQuery(insertSql, asList(toSend[0])));
            helper.expectSuccess(conn.sendQuery(insertSql, asList(toSend[1])));
            helper.expectSuccess(conn.sendQuery(insertSql, asList(toSend[2])));

            helper.expectResultSetValues(conn.sendQuery(selectSql, singletonList(10)), expect);
            helper.expectSuccess(conn.sendQuery(dropSql));
            helper.expectSuccess(conn.close());
            testFinished.complete(null);
        });
    }
}
