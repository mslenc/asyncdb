package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class JdbcGetter {
    public abstract DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException;
}
