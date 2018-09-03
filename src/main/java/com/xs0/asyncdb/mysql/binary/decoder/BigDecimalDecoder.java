package com.xs0.asyncdb.mysql.binary.decoder;

import com.xs0.asyncdb.common.general.ColumnData;
import com.xs0.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readLengthEncodedString;

public class BigDecimalDecoder implements BinaryDecoder {
    private static final BigDecimalDecoder instance = new BigDecimalDecoder();

    public static BigDecimalDecoder instance() {
        return instance;
    }

    @Override
    public BigDecimal decode(ByteBuf buffer, CodecSettings settings, ColumnData columnData) {
        return new BigDecimal(readLengthEncodedString(buffer, settings.charset()));
    }
}