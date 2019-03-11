package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;
import com.github.mslenc.asyncdb.impl.values.DbValueULong;
import com.github.mslenc.asyncdb.util.ULong;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterULong extends JdbcGetter {
    private static final JdbcGetterULong instance = new JdbcGetterULong();

    public static JdbcGetterULong instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        long val = rs.getLong(jdbcColumnIndex);
        if (rs.wasNull()) {
            return DbValueNull.instance();
        } else {
            return new DbValueULong(ULong.valueOf(val));
        }
    }
}
