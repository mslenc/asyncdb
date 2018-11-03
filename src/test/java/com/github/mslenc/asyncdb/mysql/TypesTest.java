package com.github.mslenc.asyncdb.mysql;

import com.github.mslenc.asyncdb.util.ULong;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TypesTest {
    static Consumer doubleCmp(double expected) {
        return new Consumer() {
            @Override
            public void accept(Object o) {
                assertTrue(o instanceof Double);

                double actual = (Double)o;
                assertFalse(Double.isNaN(actual));
                assertFalse(Double.isInfinite(actual));

                if (actual == expected)
                    return;

                assertEquals(expected, actual, 0.000001 * expected);
            }
        };
    }

    @Parameters(name = "{0}: {1} => {2}")
    public static Iterable<Object[]> data() {
        LocalDateTime nowDateTime = LocalDateTime.now();
        Instant nowInstant = Instant.now();
        Instant nowInstantNoMicros = Instant.ofEpochSecond(nowInstant.getEpochSecond());
        LocalDate nowDate = nowDateTime.toLocalDate();

        Object[][] data = {
            { "TINYINT", -128, -128 },
            { "TINYINT",   -1,   -1 },
            { "TINYINT",    0,    0 },
            { "TINYINT",    1,    1 },
            { "TINYINT",  127,  127 },

            { "TINYINT UNSIGNED",   0,   0 },
            { "TINYINT UNSIGNED",   1,   1 },
            { "TINYINT UNSIGNED", 127, 127 },
            { "TINYINT UNSIGNED", 128, 128 },
            { "TINYINT UNSIGNED", 255, 255 },

            { "SMALLINT", -32768, -32768 },
            { "SMALLINT", "-256", -256 },
            { "SMALLINT", new BigDecimal(-255), -255 },
            { "SMALLINT", new BigInteger("-128"), -128 },
            { "SMALLINT", -127, -127 },
            { "SMALLINT", -1, -1 },
            { "SMALLINT", 0, 0 },
            { "SMALLINT", 1, 1 },
            { "SMALLINT", 127, 127 },
            { "SMALLINT", 128, 128 },
            { "SMALLINT", 32767, 32767 },

            { "SMALLINT UNSIGNED", 0,  0 },
            { "SMALLINT UNSIGNED", 1,  1 },
            { "SMALLINT UNSIGNED", 127,  127 },
            { "SMALLINT UNSIGNED", 128,  128 },
            { "SMALLINT UNSIGNED", 32767, 32767 },
            { "SMALLINT UNSIGNED", 32768, 32768 },
            { "SMALLINT UNSIGNED", 65535, 65535 },

            { "MEDIUMINT", -8388608, -8388608 },
            { "MEDIUMINT", -1, -1 },
            { "MEDIUMINT", 0, 0 },
            { "MEDIUMINT", 1, 1 },
            { "MEDIUMINT", 8388607, 8388607 },

            { "MEDIUMINT UNSIGNED", 0, 0 },
            { "MEDIUMINT UNSIGNED", 1, 1 },
            { "MEDIUMINT UNSIGNED", 8388607, 8388607 },
            { "MEDIUMINT UNSIGNED", 8388608, 8388608 },
            { "MEDIUMINT UNSIGNED", 16777215, 16777215 },

            { "INT", 1, 1 },
            { "INT", 0, 0 },
            { "INT", -1, -1 },
            { "INT", Integer.MIN_VALUE, Integer.MIN_VALUE },
            { "INT", Integer.MAX_VALUE, Integer.MAX_VALUE },

            { "INT UNSIGNED", 1, 1L },
            { "INT UNSIGNED", Integer.MAX_VALUE, (long) Integer.MAX_VALUE },
            { "INT UNSIGNED", 0xFFFFFFFFL, 0xFFFFFFFFL },

            { "BIGINT", Long.MIN_VALUE, Long.MIN_VALUE },
            { "BIGINT", -1, -1L },
            { "BIGINT", 0, 0L },
            { "BIGINT", 1, 1L },
            { "BIGINT", Long.MAX_VALUE, Long.MAX_VALUE },
            { "BIGINT", Integer.MIN_VALUE, (long)Integer.MIN_VALUE },
            { "BIGINT", Integer.MAX_VALUE, (long)Integer.MAX_VALUE },
            { "BIGINT", new BigDecimal("1251551516153354"), 1251551516153354L },
            { "BIGINT", "987459876351", 987459876351L },

            { "BIGINT UNSIGNED", 0, ULong.valueOf(0L) },
            { "BIGINT UNSIGNED", 1, ULong.valueOf(1L) },
            { "BIGINT UNSIGNED", Long.MAX_VALUE, ULong.valueOf(Long.MAX_VALUE) },
            { "BIGINT UNSIGNED", Integer.MAX_VALUE, ULong.valueOf(Integer.MAX_VALUE) },
            { "BIGINT UNSIGNED", new BigDecimal("1251551516153354"), ULong.valueOf(1251551516153354L) },
            { "BIGINT UNSIGNED", "987459876351", ULong.valueOf(987459876351L) },
            { "BIGINT UNSIGNED", ULong.valueOf(0xFFFF_FFFF_FFFF_FFFFL), ULong.MAX_VALUE },

            // the default "FLOAT" sends one digit too few in decimal, so we add (100,30) in some cases..
            { "FLOAT", 123.456, 123.456f },
            { "FLOAT", Float.MIN_VALUE, Float.MIN_VALUE },
            { "FLOAT(100,30)", Float.MAX_VALUE, Float.MAX_VALUE },
            { "FLOAT(100,30)", Integer.MAX_VALUE, (float)Integer.MAX_VALUE },
            { "FLOAT(100,30)", Long.MAX_VALUE, (float)Long.MAX_VALUE },

            { "DOUBLE", 123.456, 123.456 },
            { "DOUBLE", Float.MIN_NORMAL, doubleCmp(Float.MIN_NORMAL) },
            { "DOUBLE", Double.MAX_VALUE, Double.MAX_VALUE },
            { "DOUBLE", Float.MAX_VALUE, doubleCmp(Float.MAX_VALUE) },
            { "DOUBLE", Integer.MAX_VALUE, (double)Integer.MAX_VALUE },
            { "DOUBLE", Long.MAX_VALUE, doubleCmp(Long.MAX_VALUE) },

            { "DECIMAL(15,5)", 123.456, new BigDecimal("123.45600") },
            { "DECIMAL(15,5)", "123.456", new BigDecimal("123.45600") },
            { "DECIMAL(15,5)", new BigDecimal(123.456), new BigDecimal("123.45600") },
            { "DECIMAL(15,5)", new BigDecimal("9999999999.99999"), new BigDecimal("9999999999.99999") },
            { "DECIMAL(15,5)", new BigDecimal("-9999999999.99999"), new BigDecimal("-9999999999.99999") },
            { "DECIMAL(15,5)", new BigInteger("1234567890"), new BigDecimal("1234567890.00000") },
            { "DECIMAL(15,5)", 0, new BigDecimal("0.00000") },
            { "DECIMAL(15,5)", Math.PI, new BigDecimal("3.14159") },

            { "SET('a', 'b', 'c', 'd')", "", "" },
            { "SET('a', 'b', 'c', 'd')", "a", "a" },
            { "SET('a', 'b', 'c', 'd')", "a,b", "a,b" },
            { "SET('a', 'b', 'c', 'd')", "b,a,c", "a,b,c" },
            { "SET('a', 'b', 'c', 'd')", "d,c,b,a", "a,b,c,d" },

            { "ENUM('a', 'b', 'c', 'd')", "a", "a" },
            { "ENUM('a', 'b', 'c', 'd')", "b", "b" },
            { "ENUM('a', 'b', 'c', 'd')", "c", "c" },
            { "ENUM('a', 'b', 'c', 'd')", "d", "d" },

            { "DATE", "2018-09-29", LocalDate.of(2018, 9, 29) },
            { "DATE", LocalDate.of(2018, 9, 28), LocalDate.of(2018, 9, 28) },
            { "DATE", Instant.ofEpochSecond(1538051157), LocalDate.of(2018, 9, 27) },
            { "DATE", LocalDateTime.of(2018, 9, 23, 12, 0, 0), LocalDate.of(2018, 9, 23) },
            { "DATE", nowDate, nowDate },
            { "DATE", nowDateTime, nowDate },

            { "DATETIME", "2018-09-29 18:05:23", LocalDateTime.of(2018, 9, 29, 18, 5, 23) },
            { "DATETIME", LocalDateTime.of(2018, 9, 29, 18, 6, 1), LocalDateTime.of(2018, 9, 29, 18, 6, 1) },
            { "DATETIME", Instant.ofEpochSecond(1538051157), LocalDateTime.of(2018, 9, 27, 12, 25, 57) },
            { "DATETIME", nowInstantNoMicros, LocalDateTime.ofInstant(nowInstantNoMicros, ZoneOffset.UTC) },
            { "DATETIME(6)", nowInstant, LocalDateTime.ofInstant(nowInstant, ZoneOffset.UTC) },

            { "TIME", "12:55:07", Duration.ofSeconds(12 * 3600 + 55 * 60 + 7) },
            { "TIME", LocalTime.of(19, 4, 57), Duration.ofSeconds(19 * 3600 + 4 * 60 + 57) },
            { "TIME", Duration.ofSeconds(-582454, 124141000), Duration.ofSeconds(-582454, 0) },
            { "TIME", Duration.ofSeconds(590587, 491872000), Duration.ofSeconds(590587, 0) },
            { "TIME(6)", Duration.ofSeconds(-582454, 502423000), Duration.ofSeconds(-582454, 502423000) },
            { "TIME(6)", Duration.ofSeconds(590587, 491872000), Duration.ofSeconds(590587, 491872000) },
            { "TIME", "-156:23:55", Duration.ofSeconds(-156*3600 - 23*60 - 55) },

            { "TIMESTAMP", "2019-09-29 17:39:24.123", LocalDateTime.of(2019, 9, 29, 17, 39, 24).toInstant(ZoneOffset.UTC) },
            { "TIMESTAMP(6)", "2019-09-29 17:39:24.123", LocalDateTime.of(2019, 9, 29, 17, 39, 24, 123000000).toInstant(ZoneOffset.UTC) },
            { "TIMESTAMP", nowInstantNoMicros, nowInstantNoMicros },
            { "TIMESTAMP(6)", nowInstant, nowInstant },

            { "YEAR", "2018", Year.of(2018) },
            { "YEAR", Year.of(2018), Year.of(2018) },
        };

        return Arrays.asList(data);
    }


    final String mySqlType;
    final Object outgoingValue;
    final Object expectedValueBack;

    public TypesTest(String mySqlType, Object outgoingValue, Object expectedValueBack) {
        this.mySqlType = mySqlType;
        this.outgoingValue = outgoingValue;
        this.expectedValueBack = expectedValueBack;
    }

    static AtomicInteger testCounter = new AtomicInteger();

    @Test
    public void testTypeConversion() {
        String tableName = "types" + testCounter.incrementAndGet();

        String dropIfExistsSql =
            "DROP TABLE IF EXISTS " + tableName;

        String createSql =
            "CREATE TABLE " + tableName + " (" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "the_value " + mySqlType +
            ")";

        String insertSql =
            "INSERT INTO " + tableName + "(the_value) VALUES (?)";

        String selectSql =
            "SELECT the_value FROM " + tableName;

        String dropSql =
            "DROP TABLE " + tableName;

        TestHelper.runTest((conn, helper, testFinished) ->
            helper.expectSuccess(conn.execute(dropIfExistsSql), ignored ->
            helper.expectSuccess(conn.execute(createSql), ignored2 ->
            helper.expectSuccess(conn.execute(insertSql, outgoingValue), ignored3 ->
            helper.expectResultSetValues(conn.executeQuery(selectSql), new Object[][] { { expectedValueBack } }, ignored5 ->
            helper.expectSuccess(conn.execute(dropSql), ignored6 ->
            helper.expectSuccess(conn.close(), ignored7 ->
            testFinished.complete(null)
        )))))));
    }

    @Test
    public void testTypeConversionWithPreparedStatements() {
        String tableName = "types" + testCounter.incrementAndGet();

        String dropIfExistsSql =
                "DROP TABLE IF EXISTS " + tableName;

        String createSql =
                "CREATE TABLE " + tableName + " (" +
                        "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "the_value " + mySqlType +
                        ")";

        String insertSql =
                "INSERT INTO " + tableName + "(the_value) VALUES (?)";

        String selectSql =
                "SELECT the_value FROM " + tableName;

        String dropSql =
                "DROP TABLE " + tableName;

        TestHelper.runTest((conn, helper, testFinished) ->
            helper.expectSuccess(conn.execute(dropIfExistsSql), ignored ->
            helper.expectSuccess(conn.execute(createSql), ignored2 ->
            helper.expectSuccess(conn.prepareStatement(insertSql), ps ->
            helper.expectSuccess(ps.executeUpdate(singletonList(outgoingValue)), ignored3 ->
            helper.expectSuccess(ps.close(), ignored4 ->
            helper.expectSuccess(conn.prepareStatement(selectSql), ps2 ->
            helper.expectResultSetValues(ps2.executeQuery(emptyList()), new Object[][] { { expectedValueBack } }, ignored5 ->
            helper.expectSuccess(ps2.close(), ignored6 ->
            helper.expectSuccess(conn.execute(dropSql), ignored7 ->
            helper.expectSuccess(conn.close(), ignored8 ->
            testFinished.complete(null)
        )))))))))));
    }
}
