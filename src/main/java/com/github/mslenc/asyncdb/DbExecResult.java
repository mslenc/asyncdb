package com.github.mslenc.asyncdb;

public interface DbExecResult extends DbUpdateResult {
    DbResultSet getResultSet();
}
