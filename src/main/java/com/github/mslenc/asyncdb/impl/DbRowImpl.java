package com.github.mslenc.asyncdb.impl;

import com.github.mslenc.asyncdb.DbColumn;
import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.DbRow;
import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public class DbRowImpl implements DbRow {
    private final int rowIndex;
    private DbValue[] values;
    private DbColumns columns;

    public DbRowImpl(int rowIndex, DbValue[] values, DbColumns columns) {
        this.rowIndex = rowIndex;
        this.values = values;
        this.columns = columns;
    }

    public static DbRowImpl copyFrom(List<DbValue> values, DbColumns columns, int rowIndex) {
        DbValue[] copy = values.toArray(new DbValue[0]);
        return new DbRowImpl(rowIndex, copy, columns);
    }

    public DbColumns getColumns() {
        return columns;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public boolean isNull(int columnIndex) {
        return values[columnIndex].isNull();
    }

    public Object get(int columnIndex) {
        return values[columnIndex].unwrap();
    }

    public DbValue getValue(int columnIndex) {
        return values[columnIndex];
    }

    public String getString(int columnIndex) {
        return values[columnIndex].asString();
    }

    public byte getByte(int columnIndex) {
        return values[columnIndex].asByte();
    }

    public short getShort(int columnIndex) {
        return values[columnIndex].asShort();
    }

    public int getInt(int columnIndex) {
        return values[columnIndex].asInt();
    }

    public long getLong(int columnIndex) {
        return values[columnIndex].asLong();
    }

    public float getFloat(int columnIndex) {
        return values[columnIndex].asFloat();
    }

    public double getDouble(int columnIndex) {
        return values[columnIndex].asDouble();
    }

    public BigDecimal getBigDecimal(int columnIndex) {
        return values[columnIndex].asBigDecimal();
    }

    public ULong getULong(int columnIndex) {
        return values[columnIndex].asULong();
    }

    public Number getNumber(int columnIndex) {
        return values[columnIndex].asNumber();
    }

    public LocalDate getLocalDate(int columnIndex) {
        return values[columnIndex].asLocalDate();
    }

    public LocalTime getLocalTime(int columnIndex) {
        return values[columnIndex].asLocalTime();
    }

    public LocalDateTime getLocalDateTime(int columnIndex) {
        return values[columnIndex].asLocalDateTime();
    }

    public Duration getDuration(int columnIndex) {
        return values[columnIndex].asDuration();
    }

    public Instant getInstant(int columnIndex) {
        return values[columnIndex].asInstant();
    }

    public Year getYear(int columnIndex) {
        return values[columnIndex].asYear();
    }

    public byte[] getByteArray(int columnIndex) {
        return values[columnIndex].asByteArray();
    }

    public boolean getBoolean(int columnIndex) {
        return values[columnIndex].asBoolean();
    }

    protected DbValue valueByName(String columnName) {
        DbColumn column = columns.get(columnName);
        if (column == null)
            throw new IllegalArgumentException("No column named " + columnName);
        
        return values[column.getIndexInRow()];
    }

    
    public boolean isNull(String columnName) {
        return valueByName(columnName).isNull();
    }

    public Object get(String columnName) {
        return valueByName(columnName).unwrap();
    }

    public DbValue getValue(String columnName) {
        return valueByName(columnName);
    }

    public String getString(String columnName) {
        return valueByName(columnName).asString();
    }

    public byte getByte(String columnName) {
        return valueByName(columnName).asByte();
    }

    public short getShort(String columnName) {
        return valueByName(columnName).asShort();
    }

    public int getInt(String columnName) {
        return valueByName(columnName).asInt();
    }

    public long getLong(String columnName) {
        return valueByName(columnName).asLong();
    }

    public float getFloat(String columnName) {
        return valueByName(columnName).asFloat();
    }

    public double getDouble(String columnName) {
        return valueByName(columnName).asDouble();
    }

    public BigDecimal getBigDecimal(String columnName) {
        return valueByName(columnName).asBigDecimal();
    }

    public ULong getULong(String columnName) {
        return valueByName(columnName).asULong();
    }

    public Number getNumber(String columnName) {
        return valueByName(columnName).asNumber();
    }

    public LocalDate getLocalDate(String columnName) {
        return valueByName(columnName).asLocalDate();
    }

    public LocalTime getLocalTime(String columnName) {
        return valueByName(columnName).asLocalTime();
    }

    public LocalDateTime getLocalDateTime(String columnName) {
        return valueByName(columnName).asLocalDateTime();
    }

    public Duration getDuration(String columnName) {
        return valueByName(columnName).asDuration();
    }

    public Instant getInstant(String columnName) {
        return valueByName(columnName).asInstant();
    }

    public Year getYear(String columnName) {
        return valueByName(columnName).asYear();
    }

    public byte[] getByteArray(String columnName) {
        return valueByName(columnName).asByteArray();
    }

    public boolean getBoolean(String columnName) {
        return valueByName(columnName).asBoolean();
    }
}
