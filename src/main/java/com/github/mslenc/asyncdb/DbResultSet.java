package com.github.mslenc.asyncdb;

import java.util.List;

public interface DbResultSet extends List<DbRow> {
    DbColumns getColumns();
}
