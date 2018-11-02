package com.github.mslenc.asyncdb.my.resultset;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.DbRowImpl;
import com.github.mslenc.asyncdb.my.MyDbColumn;
import com.github.mslenc.asyncdb.my.MyDbColumns;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;

import java.util.ArrayList;

public abstract class MyDbRowResultSetBuilder<QR> extends MyDbValueResultSetBuilder<QR> {
    protected abstract void onRow(DbRowImpl row);

    protected MyDbRowResultSetBuilder(MyEncoders encoders, MyDbColumns columns) {
        super(encoders, columns);
    }

    private int rowCount;
    private ArrayList<DbValue> currentRowValues = new ArrayList<>();

    @Override
    public void startRow() {
        currentRowValues.clear();
    }

    @Override
    public void endRow() {
        onRow(DbRowImpl.copyFrom(currentRowValues, dbColumns(), rowCount++));
    }

    @Override
    public void onDbValue(MyDbColumn column, DbValue dbValue) {
        currentRowValues.add(column.getIndexInRow(), dbValue);
    }
}
