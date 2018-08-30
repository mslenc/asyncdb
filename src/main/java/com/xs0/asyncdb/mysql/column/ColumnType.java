package com.xs0.asyncdb.mysql.column;

import java.util.HashMap;

public class ColumnType {
    // see https://github.com/mysql/mysql-connector-j/blob/9cc87a48e75c2d2e87c1a293b2862ce651cb256e/src/com/mysql/jdbc/MysqlDefs.java
    public static final int FIELD_TYPE_BIT = 16;
    public static final int FIELD_TYPE_BLOB = 252;
    public static final int FIELD_TYPE_DATE = 10;
    public static final int FIELD_TYPE_DATETIME = 12;
    public static final int FIELD_TYPE_DECIMAL = 0;
    public static final int FIELD_TYPE_NUMERIC = -10; // TODO: what is this?
    public static final int FIELD_TYPE_DOUBLE = 5;
    public static final int FIELD_TYPE_ENUM = 247;
    public static final int FIELD_TYPE_FLOAT = 4;
    public static final int FIELD_TYPE_GEOMETRY = 255;
    public static final int FIELD_TYPE_INT24 = 9;
    public static final int FIELD_TYPE_JSON = 245;
    public static final int FIELD_TYPE_LONG = 3;
    public static final int FIELD_TYPE_LONG_BLOB = 251;
    public static final int FIELD_TYPE_LONGLONG = 8;
    public static final int FIELD_TYPE_MEDIUM_BLOB = 250;
    public static final int FIELD_TYPE_NEW_DECIMAL = 246;
    public static final int FIELD_TYPE_NEWDATE = 14;
    public static final int FIELD_TYPE_NULL = 6;
    public static final int FIELD_TYPE_SET = 248;
    public static final int FIELD_TYPE_SHORT = 2;
    public static final int FIELD_TYPE_STRING = 254;
    public static final int FIELD_TYPE_TIME = 11;
    public static final int FIELD_TYPE_TIMESTAMP = 7;
    public static final int FIELD_TYPE_TINY = 1;
    public static final int FIELD_TYPE_TINY_BLOB = 249;
    public static final int FIELD_TYPE_VAR_STRING = 253;
    public static final int FIELD_TYPE_VARCHAR = 15;
    public static final int FIELD_TYPE_YEAR = 13;

    public String getMapping(int fieldType) {
        switch (fieldType) {
            case FIELD_TYPE_BIT: return "bit";
            case FIELD_TYPE_BLOB: return "blob";
            case FIELD_TYPE_DATE: return "date";
            case FIELD_TYPE_DATETIME: return "datetime";
            case FIELD_TYPE_DECIMAL: return "decimal";
            case FIELD_TYPE_NUMERIC: return "numeric";
            case FIELD_TYPE_DOUBLE: return "double";
            case FIELD_TYPE_ENUM: return "enum";
            case FIELD_TYPE_FLOAT: return "float";
            case FIELD_TYPE_GEOMETRY: return "geometry";
            case FIELD_TYPE_INT24: return "int24";
            case FIELD_TYPE_JSON: return "json";
            case FIELD_TYPE_LONG: return "integer";
            case FIELD_TYPE_LONG_BLOB: return "long_blob";
            case FIELD_TYPE_LONGLONG: return "long";
            case FIELD_TYPE_MEDIUM_BLOB: return "medium_blob";
            case FIELD_TYPE_NEW_DECIMAL: return "new_decimal";
            case FIELD_TYPE_NEWDATE: return "new_date";
            case FIELD_TYPE_NULL: return "null";
            case FIELD_TYPE_SET: return "set";
            case FIELD_TYPE_SHORT: return "short";
            case FIELD_TYPE_STRING: return "string";
            case FIELD_TYPE_TIME: return "time";
            case FIELD_TYPE_TIMESTAMP: return "timestamp";
            case FIELD_TYPE_TINY: return "tiny";
            case FIELD_TYPE_TINY_BLOB: return "tiny_blob";
            case FIELD_TYPE_VAR_STRING: return "var_string";
            case FIELD_TYPE_VARCHAR: return "varchar";
            case FIELD_TYPE_YEAR: return "year";
        }

        return null;
    }
}