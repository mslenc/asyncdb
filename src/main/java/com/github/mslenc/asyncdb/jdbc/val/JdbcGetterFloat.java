package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueFloat;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterFloat extends JdbcGetter {
    private static final JdbcGetterFloat instance = new JdbcGetterFloat();

    public static JdbcGetterFloat instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        float val = rs.getFloat(jdbcColumnIndex);
        if (rs.wasNull()) {
            return DbValueNull.instance();
        } else {
            return new DbValueFloat(val);
        }
    }
}
