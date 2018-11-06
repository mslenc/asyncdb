package com.github.mslenc.asyncdb;

import com.github.mslenc.asyncdb.util.ULong;

import java.math.BigDecimal;
import java.time.*;

public interface DbRow {
    DbColumns getColumns();

    int getRowIndex();

    DbValue getValue(int columnIndex);
    DbValue getValue(String columnName);


    default boolean isNull(int columnIndex) {
        return getValue(columnIndex).isNull();
    }

    default boolean isNull(String columnName) {
        return getValue(columnName).isNull();
    }


    default Object get(int columnIndex) {
        return getValue(columnIndex).unwrap();
    }

    default Object get(String columnName) {
        return getValue(columnName).unwrap();
    }


    default String getString(int columnIndex) {
        return getValue(columnIndex).asString();
    }

    default String getString(String columnName) {
        return getValue(columnName).asString();
    }


    default byte getByte(int columnIndex) {
        return getValue(columnIndex).asByte();
    }

    default byte getByte(String columnName) {
        return getValue(columnName).asByte();
    }


    default short getShort(int columnIndex) {
        return getValue(columnIndex).asShort();
    }

    default short getShort(String columnName) {
        return getValue(columnName).asShort();
    }


    default int getInt(int columnIndex) {
        return getValue(columnIndex).asInt();
    }

    default int getInt(String columnName) {
        return getValue(columnName).asInt();
    }


    default long getLong(int columnIndex) {
        return getValue(columnIndex).asLong();
    }

    default long getLong(String columnName) {
        return getValue(columnName).asLong();
    }


    default ULong getULong(int columnIndex) {
        return getValue(columnIndex).asULong();
    }

    default ULong getULong(String columnName) {
        return getValue(columnName).asULong();
    }


    default float getFloat(int columnIndex) {
        return getValue(columnIndex).asFloat();
    }

    default float getFloat(String columnName) {
        return getValue(columnName).asFloat();
    }


    default double getDouble(int columnIndex) {
        return getValue(columnIndex).asDouble();
    }

    default double getDouble(String columnName) {
        return getValue(columnName).asDouble();
    }


    default BigDecimal getBigDecimal(int columnIndex) {
        return getValue(columnIndex).asBigDecimal();
    }

    default BigDecimal getBigDecimal(String columnName) {
        return getValue(columnName).asBigDecimal();
    }


    default Number getNumber(int columnIndex) {
        return getValue(columnIndex).asNumber();
    }

    default Number getNumber(String columnName) {
        return getValue(columnName).asNumber();
    }


    default LocalDate getLocalDate(int columnIndex) {
        return getValue(columnIndex).asLocalDate();
    }

    default LocalDate getLocalDate(String columnName) {
        return getValue(columnName).asLocalDate();
    }


    default LocalTime getLocalTime(int columnIndex) {
        return getValue(columnIndex).asLocalTime();
    }

    default LocalTime getLocalTime(String columnName) {
        return getValue(columnName).asLocalTime();
    }


    default LocalDateTime getLocalDateTime(int columnIndex) {
        return getValue(columnIndex).asLocalDateTime();
    }

    default LocalDateTime getLocalDateTime(String columnName) {
        return getValue(columnName).asLocalDateTime();
    }


    default Duration getDuration(int columnIndex) {
        return getValue(columnIndex).asDuration();
    }

    default Duration getDuration(String columnName) {
        return getValue(columnName).asDuration();
    }


    default Instant getInstant(int columnIndex) {
        return getValue(columnIndex).asInstant();
    }

    default Instant getInstant(String columnName) {
        return getValue(columnName).asInstant();
    }

    default Year getYear(int columnIndex) {
        return getValue(columnIndex).asYear();
    }

    default Year getYear(String columnName) {
        return getValue(columnName).asYear();
    }


    default byte[] getByteArray(int columnIndex) {
        return getValue(columnIndex).asByteArray();
    }

    default byte[] getByteArray(String columnName) {
        return getValue(columnName).asByteArray();
    }


    default boolean getBoolean(int columnIndex) {
        return getValue(columnIndex).asBoolean();
    }

    default boolean getBoolean(String columnName) {
        return getValue(columnName).asBoolean();
    }
}
