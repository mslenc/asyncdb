package com.github.mslenc.asyncdb.util;

import com.github.mslenc.asyncdb.*;
import com.github.mslenc.asyncdb.impl.DbRowImpl;
import com.github.mslenc.asyncdb.impl.values.DbValueLong;

import java.util.AbstractList;

class GeneratedIdColumns extends AbstractList<DbColumn> implements DbColumns {
    static final GeneratedIdColumns INSTANCE = new GeneratedIdColumns();
    private static final DbColumn COLUMN = new DbColumn() {
        @Override
        public String getName() {
            return "<generatedId>";
        }

        @Override
        public int getIndexInRow() {
            return 0;
        }
    };

    @Override
    public DbColumn get(String columnName) {
        if ("<generatedId>".equals(columnName)) {
            return COLUMN;
        } else {
            return null;
        }
    }

    @Override
    public DbColumn get(int index) {
        if (index == 0) {
            return COLUMN;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size() {
        return 1;
    }
}

public class GeneratedIdResult extends AbstractList<DbRow> implements DbResultSet {
    private final DbRow row;

    public GeneratedIdResult(long id) {
        this.row = new DbRowImpl(0, new DbValue[] { new DbValueLong(id) }, GeneratedIdColumns.INSTANCE);
    }

    @Override
    public DbColumns getColumns() {
        return GeneratedIdColumns.INSTANCE;
    }

    @Override
    public DbRow get(int index) {
        if (index == 0) {
            return row;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size() {
        return 1;
    }
}
