package com.github.mslenc.asyncdb.jdbc.val;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.values.DbValueByteArray;
import com.github.mslenc.asyncdb.impl.values.DbValueNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcGetterBlob extends JdbcGetter {
    private static final JdbcGetterBlob instance = new JdbcGetterBlob();
    private static final int BUFF_SIZE = 4096;

    public static JdbcGetterBlob instance() {
        return instance;
    }

    @Override
    public DbValue get(ResultSet rs, int jdbcColumnIndex) throws SQLException {
        InputStream in = rs.getBinaryStream(jdbcColumnIndex);
        if (in == null)
            return DbValueNull.instance();

        try {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                byte[] buff = new byte[BUFF_SIZE];
                int read;

                while ((read = in.read(buff, 0, BUFF_SIZE)) > 0) {
                    out.write(buff, 0, read);
                }

                return new DbValueByteArray(out.toByteArray());
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }
}
