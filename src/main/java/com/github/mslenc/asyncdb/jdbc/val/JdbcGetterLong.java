package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueLong;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterLong extends JdbcGetter {
    private static final JdbcGetterLong instance = new JdbcGetterLong();

    public static JdbcGetterLong instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        long val = rs.getLong(jdbcColumnIndex);
        if (rs.wasNull()) {
            return DbValueNull.instance();
        } else {
            return new DbValueLong(val);
        }
    }
}
