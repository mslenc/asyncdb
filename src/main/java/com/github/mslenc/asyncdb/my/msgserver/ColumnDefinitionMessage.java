package com.github.mslenc.asyncdb.my.msgserver;

import com.github.mslenc.asyncdb.my.MyDbColumn;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.github.mslenc.asyncdb.util.ByteBufUtils.readBinaryLength;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readLengthEncodedString;

public class ColumnDefinitionMessage extends MyDbColumn {
    public final String catalog;
    public final String schema;
    public final String table;
    public final String originalTable;
    public final String originalName;
    public final long columnLength;
    public final byte decimals;

    public ColumnDefinitionMessage(int indexInRow, String catalog, String schema, String table, String originalTable, String name, String originalName, int characterSet, long columnLength, int columnType, int flags, byte decimals) {
        super(name, indexInRow, columnType, flags, characterSet);

        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.originalTable = originalTable;
        this.originalName = originalName;
        this.columnLength = columnLength;
        this.decimals = decimals;
    }

    public static ColumnDefinitionMessage decode(int indexInRow, ByteBuf buffer, Charset charset) {
        // https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-COM_QUERY_Response

        String catalog = readLengthEncodedString(buffer, charset);
        String schema = readLengthEncodedString(buffer, charset);
        String table = readLengthEncodedString(buffer, charset);
        String originalTable = readLengthEncodedString(buffer, charset);
        String name = readLengthEncodedString(buffer, charset);
        String originalName = readLengthEncodedString(buffer, charset);

        readBinaryLength(buffer); // supposedly always 0x0C, the length of the fields that follow

        int characterSet = buffer.readUnsignedShortLE();
        long columnLength = buffer.readUnsignedIntLE();
        int columnType = buffer.readUnsignedByte();
        int flags = buffer.readUnsignedShortLE();
        byte decimals = buffer.readByte();

        buffer.skipBytes(2); // filler

        return new ColumnDefinitionMessage(
            indexInRow,
            catalog,
            schema,
            table,
            originalTable,
            name,
            originalName,
            characterSet,
            columnLength,
            columnType,
            flags,
            decimals
        );
    }
}
