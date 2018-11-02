package com.github.mslenc.asyncdb.impl;

import com.github.mslenc.asyncdb.DbColumn;
import com.github.mslenc.asyncdb.DbColumns;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;

public class DbColumnsImpl extends AbstractList<DbColumn> implements DbColumns {
    private final DbColumn[] columns;
    private HashMap<String, DbColumn> indexByName;

    public DbColumnsImpl(List<? extends DbColumn> columns) {
        this.columns = columns.toArray(new DbColumn[0]);
    }

    public DbColumn get(int index) {
        return columns[index];
    }

    public DbColumn get(String columnName) {
        HashMap<String, DbColumn> indexByName = this.indexByName;
        if (indexByName == null) {
            indexByName = new HashMap<>();
            for (DbColumn columnDef : columns) {
                indexByName.put(columnDef.getName(), columnDef);
            }
            this.indexByName = indexByName;
        }
        return indexByName.get(columnName);
    }

    @Override
    public int size() {
        return columns.length;
    }
}
