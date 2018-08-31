package com.xs0.asyncdb.mysql.column;

public class ColumnType {
    // see https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-COM_QUERY_Response
    // see https://github.com/mysql/mysql-connector-j/blob/9cc87a48e75c2d2e87c1a293b2862ce651cb256e/src/com/mysql/jdbc/MysqlDefs.java

    public static final int FIELD_TYPE_DECIMAL = 0x00;
    public static final int FIELD_TYPE_TINY = 0x01;
    public static final int FIELD_TYPE_SHORT = 0x02;
    public static final int FIELD_TYPE_LONG = 0x03;
    public static final int FIELD_TYPE_FLOAT = 0x04;
    public static final int FIELD_TYPE_DOUBLE = 0x05;
    public static final int FIELD_TYPE_NULL = 0x06;
    public static final int FIELD_TYPE_TIMESTAMP = 0x07;
    public static final int FIELD_TYPE_LONGLONG = 0x08;
    public static final int FIELD_TYPE_INT24 = 0x09;
    public static final int FIELD_TYPE_DATE = 0x0A;
    public static final int FIELD_TYPE_TIME = 0x0B;
    public static final int FIELD_TYPE_DATETIME = 0x0C;
    public static final int FIELD_TYPE_YEAR = 0x0D;
    public static final int FIELD_TYPE_VARCHAR = 0x0F;
    public static final int FIELD_TYPE_BIT = 0x10;
    public static final int FIELD_TYPE_JSON = 0xF5;
    public static final int FIELD_TYPE_NEW_DECIMAL = 0xF6;
    public static final int FIELD_TYPE_ENUM = 0xF7;
    public static final int FIELD_TYPE_SET = 0xF8;
    public static final int FIELD_TYPE_TINY_BLOB = 0xF9;
    public static final int FIELD_TYPE_MEDIUM_BLOB = 0xFA;
    public static final int FIELD_TYPE_LONG_BLOB = 0xFB;
    public static final int FIELD_TYPE_BLOB = 0xFC;
    public static final int FIELD_TYPE_VAR_STRING = 0xFD;
    public static final int FIELD_TYPE_STRING = 0xFE;
    public static final int FIELD_TYPE_GEOMETRY = 0xFF;

    // these are server-internal..
    // public static final int FIELD_TYPE_NEWDATE = 0x0E;
    // public static final int FIELD_TYPE_TIMESTAMP2 = 0x11;
    // public static final int FIELD_TYPE_DATETIME2 = 0x12;
    // public static final int FIELD_TYPE_TIME2 = 0x13;


    public String getMapping(int fieldType) {
        switch (fieldType) {
            case FIELD_TYPE_BIT: return "bit";
            case FIELD_TYPE_BLOB: return "blob";
            case FIELD_TYPE_DATE: return "date";
            case FIELD_TYPE_DATETIME: return "datetime";
            case FIELD_TYPE_DECIMAL: return "decimal";
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