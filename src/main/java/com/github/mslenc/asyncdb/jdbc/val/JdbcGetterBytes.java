package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueByteArray;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterBytes extends JdbcGetter {
    private static final JdbcGetterBytes instance = new JdbcGetterBytes();

    public static JdbcGetterBytes instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(jdbcColumnIndex);

        if (bytes == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueByteArray(bytes);
        }
    }
}
