package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueInstant;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class JdbcGetterTimestamp extends JdbcGetter {
    private static final JdbcGetterTimestamp instance = new JdbcGetterTimestamp();

    public static JdbcGetterTimestamp instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        Timestamp val = rs.getTimestamp(jdbcColumnIndex);
        if (val == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueInstant(val.toInstant(), val.toLocalDateTime());
        }
    }
}
