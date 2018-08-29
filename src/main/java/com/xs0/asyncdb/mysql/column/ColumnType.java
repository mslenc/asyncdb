package com.xs0.asyncdb.mysql.column;

import java.util.HashMap;

public enum ColumnType {
    // see https://github.com/mysql/mysql-connector-j/blob/9cc87a48e75c2d2e87c1a293b2862ce651cb256e/src/com/mysql/jdbc/MysqlDefs.java
    FIELD_TYPE_BIT(16, "bit"),
    FIELD_TYPE_BLOB(252, "blob"),
    FIELD_TYPE_DATE(10, "date"),
    FIELD_TYPE_DATETIME(12, "datetime"),
    FIELD_TYPE_DECIMAL(0, "decimal"),
    FIELD_TYPE_NUMERIC(-10, "numeric"),
    FIELD_TYPE_DOUBLE(5, "double"),
    FIELD_TYPE_ENUM(247, "enum"),
    FIELD_TYPE_FLOAT(4, "float"),
    FIELD_TYPE_GEOMETRY(255, "geometry"),
    FIELD_TYPE_INT24(9, "int24"),
    FIELD_TYPE_JSON(245, "json"),
    FIELD_TYPE_LONG(3, "integer"),
    FIELD_TYPE_LONG_BLOB(251, "long_blob"),
    FIELD_TYPE_LONGLONG(8, "long"),
    FIELD_TYPE_MEDIUM_BLOB(250, "medium_blob"),
    FIELD_TYPE_NEW_DECIMAL(246, "new_decimal"),
    FIELD_TYPE_NEWDATE(14, "new_date"),
    FIELD_TYPE_NULL(6, "null"),
    FIELD_TYPE_SET(248, "set"),
    FIELD_TYPE_SHORT(2, "short"),
    FIELD_TYPE_STRING(254, "string"),
    FIELD_TYPE_TIME(11, "time"),
    FIELD_TYPE_TIMESTAMP(7, "timestamp"),
    FIELD_TYPE_TINY(1, "tiny"),
    FIELD_TYPE_TINY_BLOB(249, "tiny_blob"),
    FIELD_TYPE_VAR_STRING(253, "var_string"),
    FIELD_TYPE_VARCHAR(15, "varchar"),
    FIELD_TYPE_YEAR(13, "year");

    private final int number;
    private final String mapping;

    ColumnType(int number, String mapping) {
        this.number = number;
        this.mapping = mapping;
    }

    public int getNumber() {
        return number;
    }

    public String getMapping() {
        return mapping;
    }

    private static HashMap<Integer, ColumnType> index = new HashMap<>();
    static {
        for (ColumnType type : values()) {
            index.put(type.number, type);
        }
    }

    public static ColumnType getByNumber(int number) {
        return index.get(number);
    }
}