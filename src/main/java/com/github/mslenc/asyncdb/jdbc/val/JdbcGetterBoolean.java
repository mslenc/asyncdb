package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueBoolean;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterBoolean extends JdbcGetter {
    private static final JdbcGetterBoolean instance = new JdbcGetterBoolean();

    public static JdbcGetterBoolean instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        boolean val = rs.getBoolean(jdbcColumnIndex);
        if (rs.wasNull()) {
            return DbValueNull.instance();
        } else {
            return DbValueBoolean.of(val);
        }
    }
}
