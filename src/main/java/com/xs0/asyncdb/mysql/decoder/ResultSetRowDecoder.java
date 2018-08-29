package com.xs0.asyncdb.mysql.decoder;

import java.nio.charset.Charset;

import com.xs0.asyncdb.mysql.message.server.ResultSetRowMessage;
import com.xs0.asyncdb.mysql.message.server.ServerMessage;
import io.netty.buffer.ByteBuf;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.MYSQL_NULL;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readBinaryLength;


public class ResultSetRowDecoder implements MessageDecoder {
    private final Charset charset;

    public ResultSetRowDecoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ServerMessage decode(ByteBuf buffer) {
        ResultSetRowMessage row = new ResultSetRowMessage();

        while (buffer.isReadable()) {
            if (buffer.getUnsignedByte(buffer.readerIndex()) == MYSQL_NULL) {
                buffer.readByte();
                row.add(null);
            } else {
                int length = (int) readBinaryLength(buffer);
                row.add(buffer.readBytes(length));
            }
        }

        return row;
    }
}
