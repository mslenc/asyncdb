package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueLocalTime;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class JdbcGetterTime extends JdbcGetter {
    private static final JdbcGetterTime instance = new JdbcGetterTime();

    public static JdbcGetterTime instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        Time val = rs.getTime(jdbcColumnIndex);
        if (val == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueLocalTime(val.toLocalTime());
        }
    }
}
