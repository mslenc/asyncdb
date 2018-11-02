package com.github.mslenc.asyncdb.my;

import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.impl.DbColumnsImpl;
import com.github.mslenc.asyncdb.my.msgserver.ColumnDefinitionMessage;

import java.util.AbstractList;
import java.util.List;

public class MyDbColumns extends AbstractList<MyDbColumn> {
    private final List<? extends MyDbColumn> columns;

    public MyDbColumns(List<? extends MyDbColumn> columns) {
        this.columns = columns;
    }

    public static MyDbColumns create(List<ColumnDefinitionMessage> columnDefs) {
        return new MyDbColumns(columnDefs);
    }

    @Override
    public MyDbColumn get(int index) {
        return columns.get(index);
    }

    @Override
    public int size() {
        return columns.size();
    }

    public DbColumns toDbColumns() {
        return new DbColumnsImpl(columns);
    }
}
