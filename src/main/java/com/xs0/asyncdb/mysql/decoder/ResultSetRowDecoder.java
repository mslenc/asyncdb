package com.xs0.asyncdb.mysql.decoder;

import com.xs0.asyncdb.mysql.message.server.ResultSetRowMessage;
import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readBinaryLength;
import static com.xs0.asyncdb.mysql.util.MySQLIO.TEXT_RESULTSET_NULL;


public class ResultSetRowDecoder {
    public static ResultSetRowMessage decode(ByteBuf buffer) {
        ResultSetRowMessage row = new ResultSetRowMessage();

        while (buffer.isReadable()) {
            int firstByte = buffer.readUnsignedByte();

            if (firstByte == TEXT_RESULTSET_NULL) {
                row.add(null);
            } else {
                int length = (int) readBinaryLength(firstByte, buffer);
                row.add(buffer.readRetainedSlice(length));
            }
        }

        return row;
    }

    // this should match what the decode above is doing..
    public static void releaseBufs(ResultSetRowMessage rowMsg) {
        for (ByteBuf buf : rowMsg)
            if (buf != null)
                buf.release();
    }
}
