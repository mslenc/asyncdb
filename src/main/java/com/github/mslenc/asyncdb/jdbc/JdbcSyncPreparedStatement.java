package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.impl.DbQueryResultImpl;
import com.github.mslenc.asyncdb.util.EmptyResultSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.github.mslenc.asyncdb.jdbc.JdbcUtils.extractGeneratedKeys;
import static com.github.mslenc.asyncdb.jdbc.JdbcUtils.setValues;
import static java.util.Arrays.asList;

public class JdbcSyncPreparedStatement implements AutoCloseable {
    final PreparedStatement ps;
    final DbColumns columns;
    final DbColumns parameters;

    public JdbcSyncPreparedStatement(PreparedStatement ps, DbColumns columns, DbColumns parameters) {
        this.ps = ps;
        this.columns = columns;
        this.parameters = parameters;
    }

    public DbColumns getColumns() {
        return columns;
        // return JdbcColumns.extractColumns(ps.getMetaData());
    }

    public DbColumns getParameters() {
        return parameters;
        // return JdbcColumns.extractColumns(ps.getParameterMetaData());
    }

    public DbResultSet executeQuery(List<Object> values) throws SQLException {
        setValues(ps, values);

        try (ResultSet rs = ps.executeQuery()) {
            return JdbcUtils.extractResultSet(rs);
        }
    }

    public DbResultSet executeQuery(Object... values) throws SQLException {
        return executeQuery(asList(values));
    }


    public DbUpdateResult executeUpdate(List<Object> values) throws SQLException {
        setValues(ps, values);
        int rowsAffected = ps.executeUpdate();
        DbResultSet generatedKeys = rowsAffected > 0 ? extractGeneratedKeys(ps) : EmptyResultSet.INSTANCE;
        return new DbQueryResultImpl(rowsAffected, null, null, generatedKeys);
    }

    public DbUpdateResult executeUpdate(Object... values) throws SQLException {
        return executeUpdate(asList(values));
    }


    public DbExecResult execute(List<Object> values) throws SQLException {
        setValues(ps, values);

        if (ps.execute()) {
            try (ResultSet rs = ps.getResultSet()) {
                DbResultSet resultSet = JdbcUtils.extractResultSet(rs);
                return new DbQueryResultImpl(0, null, resultSet, null);
            }
        } else {
            int rowsAffected = ps.getUpdateCount();
            DbResultSet generatedKeys = rowsAffected > 0 ? extractGeneratedKeys(ps) : EmptyResultSet.INSTANCE;
            return new DbQueryResultImpl(rowsAffected, null, null, generatedKeys);
        }
    }

    public DbExecResult execute(Object... values) throws SQLException {
        return execute(asList(values));
    }




    public void streamQuery(DbQueryResultObserver streamHandler, List<Object> values) {
        ResultSet rs;
        try {
            JdbcUtils.setValues(ps, values);

            rs = ps.executeQuery();
        } catch (Throwable e) {
            streamHandler.onError(e);
            return;
        }

        try {
            JdbcColumns columns = JdbcColumns.extractColumns(rs.getMetaData());

            int rowIndex = 0;
            while (rs.next()) {
                DbRow row = columns.extractRow(rs, rowIndex++);
                streamHandler.onNext(row);
            }
        } catch (Throwable t) {
            streamHandler.onError(t);
            return;
        } finally {
            JdbcUtils.closeSilently(rs);
        }

        streamHandler.onCompleted();
    }

    public void streamQuery(DbQueryResultObserver streamHandler, Object... values) {
        streamQuery(streamHandler, asList(values));
    }

    public void close() throws SQLException {
        ps.close();
    }
}
