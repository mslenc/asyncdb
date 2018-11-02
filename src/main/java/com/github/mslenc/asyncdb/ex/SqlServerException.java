package com.github.mslenc.asyncdb.ex;

/**
 * An exception resulting from an error reported by the database server.
 * Contains an SQL state code (see <a href="https://en.wikipedia.org/wiki/SQLSTATE">Wikipedia</a>),
 * and a vendor-specific error code.
 */
public class SqlServerException extends DatabaseException {
    private final String sqlState;
    private final int vendorCode;

    public SqlServerException(String reason, String sqlState, int vendorCode) {
        super(reason);

        this.sqlState = sqlState;
        this.vendorCode = vendorCode;
    }

    public SqlServerException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, cause);

        this.sqlState = sqlState;
        this.vendorCode = vendorCode;
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getVendorCode() {
        return vendorCode;
    }

}
