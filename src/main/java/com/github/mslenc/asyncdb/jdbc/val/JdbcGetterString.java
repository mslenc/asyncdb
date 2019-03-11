package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;
import com.github.mslenc.asyncdb.impl.values.DbValueString;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterString extends JdbcGetter {
    private static final JdbcGetterString instance = new JdbcGetterString();

    public static JdbcGetterString instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        String val = rs.getString(jdbcColumnIndex);
        if (val == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueString(val);
        }
    }
}
