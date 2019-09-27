package com.github.mslenc.asyncdb;

import org.junit.Test;

import static com.github.mslenc.asyncdb.DbType.MYSQL;
import static com.github.mslenc.asyncdb.DbTxIsolation.SERIALIZABLE;
import static com.github.mslenc.asyncdb.DbTxMode.READ_ONLY;
import static org.junit.Assert.*;

public class DbConfigTest {
    @Test
    public void testDefaultInitStatements() {
        DbConfig config = DbConfig.newBuilder(MYSQL).build();

        assertEquals(2, config.initStatements().size());
        assertEquals(DbConfig.DEFAULT_MYSQL_INIT_SQL, config.initStatements().get(0));
        assertEquals("SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ", config.initStatements().get(1));

        DbConfig config2 = config.toBuilder().setDefaultTxIsolation(SERIALIZABLE).setDefaultTxMode(READ_ONLY).build();

        assertEquals(2, config2.initStatements().size());
        assertEquals(DbConfig.DEFAULT_MYSQL_INIT_SQL, config2.initStatements().get(0));
        assertEquals("SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE", config2.initStatements().get(1));

        DbConfig config3 = config2.toBuilder().addInitStatement("STILL HAVE DEFAULTS").build();
        assertEquals(3, config3.initStatements().size());
        assertEquals(DbConfig.DEFAULT_MYSQL_INIT_SQL, config3.initStatements().get(0));
        assertEquals("SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE", config3.initStatements().get(1));
        assertEquals("STILL HAVE DEFAULTS", config3.initStatements().get(2));

        DbConfig config4 = config3.toBuilder().setInitStatements().build();

        assertEquals(0, config4.initStatements().size());

        DbConfig config5 = config4.toBuilder().setInitStatements("NO MORE DEFAULTS!").build();

        assertEquals(1, config5.initStatements().size());
        assertEquals("NO MORE DEFAULTS!", config5.initStatements().get(0));
    }
}