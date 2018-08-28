package com.xs0.asyncdb.mysql.message.server;

import com.xs0.asyncdb.common.column.ColumnDecoder;
import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.binary.decoder.BinaryDecoder;

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
    public final short flags;
    public final byte decimals;
    public final BinaryDecoder binaryDecoder;
    public final ColumnDecoder textDecoder;

    public ColumnDefinitionMessage(String catalog, String schema, String table, String originalTable, String name, String originalName, int characterSet, long columnLength, int columnType, short flags, byte decimals, BinaryDecoder binaryDecoder, ColumnDecoder textDecoder) {
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
    public int kind() {
        return ColumnDefinition;
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
}
