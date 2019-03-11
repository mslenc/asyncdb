package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;
import com.github.mslenc.asyncdb.impl.values.DbValueOffsetTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetTime;

public class JdbcGetterTZTime extends JdbcGetter {
    private static final JdbcGetterTZTime instance = new JdbcGetterTZTime();

    public static JdbcGetterTZTime instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        OffsetTime val;
        try  {
            val = rs.getObject(jdbcColumnIndex, OffsetTime.class);
        } catch (SQLException e) {
            try {
                String str = rs.getString(jdbcColumnIndex);
                if (str == null)
                    return DbValueNull.instance();

                val = OffsetTime.parse(str);
            } catch (Exception e2) {
                throw e;
            }
        }

        if (val == null) {
            return DbValueNull.instance();
        } else {
            return new DbValueOffsetTime(val);
        }
    }
}
