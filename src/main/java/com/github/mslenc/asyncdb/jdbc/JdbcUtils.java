package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.DbRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcUtils {
    private static final Logger log = LoggerFactory.getLogger(JdbcUtils.class);

    public static JdbcResultSet extractResultSet(ResultSet rs) throws SQLException {
        JdbcColumns columns = JdbcColumns.extractColumns(rs.getMetaData());

        ArrayList<DbRow> rows = new ArrayList<>();

        int rowIndex = 0;
        while (rs.next()) {
            rows.add(columns.extractRow(rs, rowIndex++));
        }

        return new JdbcResultSet(columns, rows);
    }

    public static void setValues(PreparedStatement ps, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++)
            ps.setObject(i + 1, values.get(i));
    }

    public static List<Long> extractGeneratedKeys(Statement stmt) throws SQLException {
        // we mostly expect 0 or 1 ids, so we optimize for those two cases..

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (!rs.next())
                return Collections.emptyList();

            long first = rs.getLong(1);

            if (!rs.next())
                return Collections.singletonList(first);

            ArrayList<Long> ids = new ArrayList<>();
            ids.add(first);
            ids.add(rs.getLong(1));

            while (rs.next()) {
                ids.add(rs.getLong(1));
            }

            return ids;
        }
    }

    public static void closeSilently(Statement stmt) {
        try {
            stmt.close();
        } catch (Throwable t) {
            log.error("Error while closing Statement silently", t);
        }
    }

    public static void closeSilently(ResultSet rs) {
        try {
            rs.close();
        } catch (Throwable t) {
            log.error("Error while closing ResultSet silently", t);
        }
    }
}
