package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueDouble;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterDouble extends JdbcGetter {
    private static final JdbcGetterDouble instance = new JdbcGetterDouble();

    public static JdbcGetterDouble instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        double val = rs.getDouble(jdbcColumnIndex);
        if (rs.wasNull()) {
            return DbValueNull.instance();
        } else {
            return new DbValueDouble(val);
        }
    }
}
