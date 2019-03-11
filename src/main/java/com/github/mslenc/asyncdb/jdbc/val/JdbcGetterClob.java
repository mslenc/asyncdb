package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;
import com.github.mslenc.asyncdb.impl.values.DbValueString;

import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterClob extends JdbcGetter {
    private static final JdbcGetterClob instance = new JdbcGetterClob();
    private static final int BUFF_SIZE = 4096;

    public static JdbcGetterClob instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        Reader in = rs.getCharacterStream(jdbcColumnIndex);
        if (in == null)
            return DbValueNull.instance();

        try {
            try {
                StringBuilder out = new StringBuilder();

                char[] buff = new char[BUFF_SIZE];
                int read;

                while ((read = in.read(buff, 0, BUFF_SIZE)) > 0) {
                    out.append(buff, 0, read);
                }

                return new DbValueString(out.toString());
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }
}
