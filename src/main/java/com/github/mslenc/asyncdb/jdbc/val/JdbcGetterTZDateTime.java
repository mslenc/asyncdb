package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;
import com.github.mslenc.asyncdb.impl.values.DbValueOffsetDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class JdbcGetterTZDateTime extends JdbcGetter {
    private static final JdbcGetterTZDateTime instance = new JdbcGetterTZDateTime();

    public static JdbcGetterTZDateTime instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        OffsetDateTime val;
        try  {
            val = rs.getObject(jdbcColumnIndex, OffsetDateTime.class);
        } catch (SQLException e) {
            try {
                String str = rs.getString(jdbcColumnIndex);
                if (str == null)
                    return DbValueNull.instance();

                val = OffsetDateTime.parse(str);
            } catch (Exception e2) {
                throw e;
            }
        }

        if (val == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueOffsetDateTime(val);
        }
    }
}
