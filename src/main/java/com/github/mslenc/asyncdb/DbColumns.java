package com.github.mslenc.asyncdb;

import java.util.List;

public interface DbColumns extends List<DbColumn> {
    @Override
    DbColumn get(int index);

    DbColumn get(String columnName);
}
