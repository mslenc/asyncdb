package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.impl.DbQueryResultImpl;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.github.mslenc.asyncdb.jdbc.JdbcUtils.extractGeneratedKeys;

public class JdbcSyncConnection {
    private final DbConfig config;
    private Consumer<Connection> closeHandler;
    private Connection _conn; // obtain via getConnectionCheckClosed()

    public JdbcSyncConnection(DbConfig config, Connection conn, Consumer<Connection> closeHandler) {
        this.config = config;
        this._conn = conn;
        this.closeHandler = closeHandler;
    }

    public void close() throws SQLException {
        // we make sure to close the connection only once, as well as to only call the close handler once
        Consumer<Connection> closeHandler = this.closeHandler;
        this.closeHandler = null;

        Connection conn = this._conn;
        this._conn = null;

        if (closeHandler != null) {
            closeHandler.accept(conn);
        } else {
            throw new SQLException("The connection was already closed");
        }
    }

    private Connection getConnectionCheckClosed() throws SQLException {
        Connection conn = this._conn;
        if (conn != null)
            return conn;
        throw new SQLException("The connection was already closed");
    }

    public DbConfig getConfig() {
        return config;
    }

    public DbResultSet executeQuery(String sql) throws SQLException {
        try (Statement stmt = getConnectionCheckClosed().createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                return JdbcUtils.extractResultSet(rs);
            }
        }
    }

    public DbResultSet executeQuery(String sql, List<Object> values) throws SQLException {
        if (values.isEmpty())
            return executeQuery(sql);

        try (JdbcSyncPreparedStatement ps = prepareStatement(sql)) {
            return ps.executeQuery(values);
        }
    }

    public DbResultSet executeQuery(String sql, Object... values) throws SQLException {
        if (values.length == 0)
            return executeQuery(sql);

        return executeQuery(sql, Arrays.asList(values));
    }


    public DbUpdateResult executeUpdate(String sql) throws SQLException {
        try (Statement stmt = getConnectionCheckClosed().createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            DbResultSet generatedKeys = rowsAffected > 0 ? extractGeneratedKeys(stmt) : null;

            return new DbQueryResultImpl(rowsAffected, null, null, generatedKeys);
        }
    }

    public DbUpdateResult executeUpdate(String sql, List<Object> values) throws SQLException {
        if (values.isEmpty())
            return executeUpdate(sql);

        try (JdbcSyncPreparedStatement ps = prepareStatement(sql)) {
            return ps.executeUpdate(values);
        }
    }

    public DbUpdateResult executeUpdate(String sql, Object... values) throws SQLException {
        if (values.length == 0)
            return executeUpdate(sql);

        return executeUpdate(sql, Arrays.asList(values));
    }


    void streamQuery(String sql, DbQueryResultObserver streamHandler) {
        Statement stmt;
        try {
            stmt = getConnectionCheckClosed().createStatement();
        } catch (Throwable e) {
            streamHandler.onError(e);
            return;
        }

        try {
            ResultSet rs;
            try {
                rs = stmt.executeQuery(sql);
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
        } finally {
            JdbcUtils.closeSilently(stmt);
        }

        streamHandler.onCompleted();
    }


    void streamQuery(String sql, DbQueryResultObserver streamHandler, List<Object> values) {
        if (values.isEmpty()) {
            streamQuery(sql, streamHandler);
            return;
        }

        JdbcSyncPreparedStatement ps;
        try {
            ps = prepareStatement(sql);
        } catch (Throwable t) {
            streamHandler.onError(t);
            return;
        }

        try {
            ps.streamQuery(streamHandler, values);
        } finally {
            JdbcUtils.closeSilently(ps.ps);
        }
    }


    public void streamQuery(String sql, DbQueryResultObserver streamHandler, Object... values) {
        if (values.length == 0) {
            streamQuery(sql, streamHandler);
        } else {
            streamQuery(sql, streamHandler, Arrays.asList(values));
        }
    }

    public JdbcSyncPreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement ps = getConnectionCheckClosed().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        JdbcColumns columns = JdbcColumns.extractColumns(ps.getMetaData());
        JdbcColumns parameters = JdbcColumns.extractColumns(ps.getParameterMetaData());
        return new JdbcSyncPreparedStatement(ps, columns, parameters);
    }


    public DbExecResult execute(String sql) throws SQLException {
        try (Statement stmt = getConnectionCheckClosed().createStatement()) {
            if (stmt.execute(sql)) {
                try (ResultSet rs = stmt.getResultSet()) {
                    DbResultSet resultSet = JdbcUtils.extractResultSet(rs);
                    return new DbQueryResultImpl(0, null, resultSet, null);
                }
            } else {
                int rowsAffected = stmt.getUpdateCount();
                DbResultSet generatedKeys = rowsAffected > 0 ? extractGeneratedKeys(stmt) : null;
                return new DbQueryResultImpl(rowsAffected, null, null, generatedKeys);
            }
        }
    }

    public DbExecResult execute(String sql, List<Object> values) throws SQLException {
        if (values.isEmpty())
            return execute(sql);

        try (JdbcSyncPreparedStatement ps = prepareStatement(sql)) {
            return ps.execute(values);
        }
    }

    public DbExecResult execute(String sql, Object... values) throws SQLException {
        if (values.length == 0) {
            return execute(sql);
        } else {
            return execute(sql, Arrays.asList(values));
        }
    }

    public void startTransaction() throws SQLException {
        DbConfig config = getConfig();
        startTransaction(config.defaultTxIsolation(), config.defaultTxMode());
    }

    public void startTransaction(DbTxIsolation isolation) throws SQLException {
        startTransaction(isolation, getConfig().defaultTxMode());
    }

    public void startTransaction(DbTxMode mode) throws SQLException {
        startTransaction(getConfig().defaultTxIsolation(), mode);
    }

    public void startTransaction(DbTxIsolation isolation, DbTxMode mode) throws SQLException {
        Connection conn = getConnectionCheckClosed();

        switch (isolation) {
            case DEFAULT:
                startTransaction(mode); // (read from config and come back here)
                return;

            case READ_UNCOMMITTED:
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                break;

            case READ_COMMITTED:
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                break;

            case REPEATABLE_READ:
                conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                break;

            case SERIALIZABLE:
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                break;
        }

        // mode is currently ignored

        conn.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        Connection conn = getConnectionCheckClosed();
        conn.commit();
        conn.setAutoCommit(true);
    }

    public void commitAndChain() throws SQLException {
        Connection conn = getConnectionCheckClosed();
        conn.commit();
        // (but we leave autocommit off)
    }

    public void rollback() throws SQLException {
        Connection conn = getConnectionCheckClosed();
        conn.rollback();
        conn.setAutoCommit(true);
    }

    public void rollbackAndChain() throws SQLException {
        Connection conn = getConnectionCheckClosed();
        conn.rollback();
        // (but we leave autocommit off)
    }
}
