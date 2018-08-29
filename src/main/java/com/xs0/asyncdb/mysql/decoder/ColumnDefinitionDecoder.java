package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.codec.DecoderRegistry;
import com.xs0.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readBinaryLength;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readLengthEncodedString;

public class ColumnDefinitionDecoder implements MessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(ColumnDefinitionDecoder.class);

    private final Charset charset;
    private final DecoderRegistry registry;

    public ColumnDefinitionDecoder(Charset charset, DecoderRegistry registry) {
        this.charset = charset;
        this.registry = registry;
    }

    @Override
    public ColumnDefinitionMessage decode(ByteBuf buffer) {
        String catalog = readLengthEncodedString(buffer, charset);
        String schema = readLengthEncodedString(buffer, charset);
        String table = readLengthEncodedString(buffer, charset);
        String originalTable = readLengthEncodedString(buffer, charset);
        String name = readLengthEncodedString(buffer, charset);
        String originalName = readLengthEncodedString(buffer, charset);

        readBinaryLength(buffer);

        int characterSet = buffer.readUnsignedShort();
        long columnLength = buffer.readUnsignedInt();
        int columnType = buffer.readUnsignedByte();
        short flags = buffer.readShort();
        byte decimals = buffer.readByte();

        buffer.readShort();

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
            registry.binaryDecoderFor(columnType, characterSet),
            registry.textDecoderFor(columnType,characterSet)
        );
    }
}