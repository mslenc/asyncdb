package com.github.mslenc.asyncdb.mysql.decoder;

import com.github.mslenc.asyncdb.mysql.message.server.ErrorMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.readFixedString;
import static com.github.mslenc.asyncdb.mysql.binary.ByteBufUtils.readUntilEOF;
import static com.github.mslenc.asyncdb.mysql.util.MySQLIO.CLIENT_PROTOCOL_41;

public class ErrorDecoder {
    // https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html

    public static ErrorMessage decodeAfterHeader(ByteBuf buffer, Charset charset, int capabilities) {
        int errorCode = buffer.readUnsignedShortLE();

        String sqlState;
        if ((capabilities & CLIENT_PROTOCOL_41) != 0) {
            buffer.readByte(); // skip '#'
            sqlState = readFixedString(buffer, 5, charset);
        } else {
            sqlState = null;
        }

        String errorMessage = readUntilEOF(buffer, charset);

        return new ErrorMessage(errorCode, sqlState, errorMessage);
    }
}