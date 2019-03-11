package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueBigDecimal;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterBigDecimal extends JdbcGetter {
    private static final JdbcGetterBigDecimal instance = new JdbcGetterBigDecimal();

    public static JdbcGetterBigDecimal instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        BigDecimal val = rs.getBigDecimal(jdbcColumnIndex);
        if (val == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueBigDecimal(val);
        }
    }
}
