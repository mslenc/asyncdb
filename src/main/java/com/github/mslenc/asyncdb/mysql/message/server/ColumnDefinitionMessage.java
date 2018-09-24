package com.github.mslenc.asyncdb.mysql.message.server;

import com.github.mslenc.asyncdb.common.general.ColumnData;
import com.github.mslenc.asyncdb.mysql.binary.decoder.BinaryDecoder;
import com.github.mslenc.asyncdb.mysql.util.MySQLIO;
import com.github.mslenc.asyncdb.common.column.ColumnDecoder;

public class ColumnDefinitionMessage implements ServerMessage, ColumnData {
    public final String catalog;
    public final String schema;
    public final String table;
    public final String originalTable;
    public final String name;
    public final String originalName;
    public final int characterSet;
    public final long columnLength;
    public final int columnType;
    public final int flags;
    public final byte decimals;
    public final BinaryDecoder binaryDecoder;
    public final ColumnDecoder textDecoder;

    public ColumnDefinitionMessage(String catalog, String schema, String table, String originalTable, String name, String originalName, int characterSet, long columnLength, int columnType, int flags, byte decimals, BinaryDecoder binaryDecoder, ColumnDecoder textDecoder) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.originalTable = originalTable;
        this.name = name;
        this.originalName = originalName;
        this.characterSet = characterSet;
        this.columnLength = columnLength;
        this.columnType = columnType;
        this.flags = flags;
        this.decimals = decimals;
        this.binaryDecoder = binaryDecoder;
        this.textDecoder = textDecoder;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int dataType() {
        return columnType;
    }

    @Override
    public long dataTypeSize() {
        return columnLength;
    }

    @Override
    public boolean isUnsigned() {
        return (flags & MySQLIO.FIELD_FLAG_UNSIGNED) != 0;
    }
}
