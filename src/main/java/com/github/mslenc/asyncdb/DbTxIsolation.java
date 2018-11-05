package com.github.mslenc.asyncdb;

public enum DbTxIsolation {
    DEFAULT("DEFAULT"),
    READ_UNCOMMITTED("READ UNCOMMITTED"),
    READ_COMMITTED("READ COMMITTED"),
    REPEATABLE_READ("REPEATABLE READ"),
    SERIALIZABLE("SERIALIZABLE");

    private final String sqlRep;

    DbTxIsolation(String sqlRep) {
        this.sqlRep = sqlRep;
    }

    @Override
    public String toString() {
        return sqlRep;
    }
}
