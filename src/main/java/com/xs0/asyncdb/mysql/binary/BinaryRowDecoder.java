package com.xs0.asyncdb.mysql.binary;

import com.xs0.asyncdb.common.exceptions.BufferNotFullyConsumedException;
import com.xs0.asyncdb.mysql.message.server.ColumnDefinitionMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BinaryRowDecoder {
    private static final Logger log = LoggerFactory.getLogger(BinaryRowDecoder.class);
    private static final int BITMAP_OFFSET = 9;

    private static final BinaryRowDecoder instance = new BinaryRowDecoder();

    public static BinaryRowDecoder instance() {
        return instance;
    }

    public Object[] decode(ByteBuf buffer, List<ColumnDefinitionMessage> columns) {
        //log.debug("columns are {} - {}", buffer.readableBytes(), columns)
        //log.debug( "decoding row\n{}", MySQLHelper.dumpAsHex(buffer))
        //PrintUtils.printArray("bitmap", buffer)

        buffer.readByte(); // header

        int nullCount = (columns.size() + 9) / 8;

        byte[] nullBitMask = new byte[nullCount];
        buffer.readBytes(nullBitMask);

        int nullMaskPos = 0;
        int bit = 4;

        Object[] row = new Object[columns.size()];

        int index = 0;

        while (index < columns.size()) {
            if ((nullBitMask[nullMaskPos] & bit) != 0) {
                row[index] = null;
            } else {
                ColumnDefinitionMessage column = columns.get(index);

                //log.debug(s"${decoder.getClass.getSimpleName} - ${buffer.readableBytes()}")
                //log.debug("Column value [{}] - {}", value, column.name)

                row[index] = column.binaryDecoder.decode(buffer);
            }

            bit <<= 1;
            if ((bit & 0xff) == 0) {
                bit = 1;
                nullMaskPos++;
            }

            index++;
        }

        // log.debug("values are {}", row)

        if (buffer.readableBytes() != 0) {
            throw new BufferNotFullyConsumedException(buffer);
        }

        return row;
    }
}