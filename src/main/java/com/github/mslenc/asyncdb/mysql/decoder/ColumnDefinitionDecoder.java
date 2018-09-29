package com.github.mslenc.asyncdb.mysql.decoder;

import com.github.mslenc.asyncdb.mysql.codec.DecoderRegistry;
import com.github.mslenc.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.readBinaryLength;
import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.readLengthEncodedString;

public class ColumnDefinitionDecoder {
    public static ColumnDefinitionMessage decode(ByteBuf buffer, Charset charset, DecoderRegistry registry) {
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

        buffer.readShortLE(); // filler

        return new ColumnDefinitionMessage(
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
            decimals,
            registry.binaryDecoderFor(columnType, characterSet, flags),
            registry.textDecoderFor(columnType,characterSet, flags)
        );
    }
}