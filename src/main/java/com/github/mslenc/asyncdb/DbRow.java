package com.github.mslenc.asyncdb;

import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;
import java.time.*;

public interface DbRow {
    DbColumns getColumns();

    int getRowIndex();

    boolean isNull(int columnIndex);
    boolean isNull(String columnName);

    Object get(int columnIndex);
    Object get(String columnName);

    DbValue getValue(int columnIndex);
    DbValue getValue(String columnName);

    String getString(int columnIndex);
    String getString(String columnName);

    byte getByte(int columnIndex);
    byte getByte(String columnName);

    short getShort(int columnIndex);
    short getShort(String columnName);

    int getInt(int columnIndex);
    int getInt(String columnName);

    long getLong(int columnIndex);
    long getLong(String columnName);

    ULong getULong(int columnIndex);
    ULong getULong(String columnName);

    float getFloat(int columnIndex);
    float getFloat(String columnName);

    double getDouble(String columnName);
    double getDouble(int columnIndex);

    BigDecimal getBigDecimal(int columnIndex);
    BigDecimal getBigDecimal(String columnName);

    Number getNumber(String columnName);
    Number getNumber(int columnIndex);

    LocalDate getLocalDate(int columnIndex);
    LocalDate getLocalDate(String columnName);

    LocalTime getLocalTime(String columnName);
    LocalTime getLocalTime(int columnIndex);

    LocalDateTime getLocalDateTime(int columnIndex);
    LocalDateTime getLocalDateTime(String columnName);

    Duration getDuration(String columnName);
    Duration getDuration(int columnIndex);

    Instant getInstant(int columnIndex);
    Instant getInstant(String columnName);

    Year getYear(String columnName);
    Year getYear(int columnIndex);

    byte[] getByteArray(int columnIndex);
    byte[] getByteArray(String columnName);

    boolean getBoolean(int columnIndex);
    boolean getBoolean(String columnName);
}
