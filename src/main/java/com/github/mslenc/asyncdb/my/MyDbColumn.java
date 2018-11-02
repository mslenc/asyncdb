package com.github.mslenc.asyncdb.my;

import com.github.mslenc.asyncdb.DbColumn;

public class MyDbColumn implements DbColumn {
    private final String name;
    private final int indexInRow;
    private final int dataType;
    private final int flags;
    private final int charsetId;

    protected MyDbColumn(String name, int indexInRow, int dataType, int flags, int charsetId) {
        this.name = name;
        this.indexInRow = indexInRow;
        this.dataType = dataType;
        this.flags = flags;
        this.charsetId = charsetId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getIndexInRow() {
        return indexInRow;
    }

    public int getDataType() {
        return dataType;
    }

    public boolean isUnsigned() {
        return hasFlag(MyConstants.FIELD_FLAG_UNSIGNED);
    }

    public int getCharsetId() {
        return charsetId;
    }

    public boolean hasFlag(int fieldFlag) {
        return (flags & fieldFlag) != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        MyDbColumn other = (MyDbColumn) o;
        return indexInRow == other.indexInRow &&
                dataType == other.dataType &&
                flags == other.flags &&
                charsetId == other.charsetId &&
                name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int res = name.hashCode();
        res = res * 31 + indexInRow;
        res = res * 31 + dataType;
        res = res * 31 + flags;
        return res * 31 + charsetId;
    }
}
