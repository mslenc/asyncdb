package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.DbResultSet;
import com.github.mslenc.asyncdb.DbRow;
import com.github.mslenc.asyncdb.impl.DbResultSetImpl;
import com.github.mslenc.asyncdb.util.EmptyResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
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

    public static DbResultSet extractGeneratedKeys(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (!rs.next())
                return EmptyResultSet.INSTANCE;

            JdbcColumns columns = JdbcColumns.extractColumns(rs.getMetaData());

            DbRow firstRow = columns.extractRow(rs, 0);

            if (!rs.next())
                return new DbResultSetImpl(columns, Collections.singletonList(firstRow));

            ArrayList<DbRow> rows = new ArrayList<>();
            rows.add(firstRow);
            rows.add(columns.extractRow(rs, 1));

            int rowIndex = 2;
            while (rs.next()) {
                rows.add(columns.extractRow(rs, rowIndex++));
            }

            return new JdbcResultSet(columns, rows);
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
