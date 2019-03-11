package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueBigDecimal;
import com.github.mslenc.asyncdb.impl.values.DbValueLocalDate;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class JdbcGetterDate extends JdbcGetter {
    private static final JdbcGetterDate instance = new JdbcGetterDate();

    public static JdbcGetterDate instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        Date val = rs.getDate(jdbcColumnIndex);
        if (val == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueLocalDate(val.toLocalDate());
        }
    }
}
