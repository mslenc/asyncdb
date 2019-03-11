package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterNull extends JdbcGetter {
    private static final JdbcGetterNull instance = new JdbcGetterNull();

    public static JdbcGetterNull instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        return DbValueNull.instance();
    }
}
