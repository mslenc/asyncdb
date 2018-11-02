package com.github.mslenc.asyncdb.my.msgserver;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static com.github.mslenc.asyncdb.my.MyConstants.CLIENT_PROTOCOL_41;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readFixedString;
import static com.github.mslenc.asyncdb.util.ByteBufUtils.readUntilEOF;

public class ErrorMessage {
    public final int errorCode;
    public final String sqlState;
    public final String errorMessage;

    public ErrorMessage(int errorCode, String sqlState, String errorMessage) {
        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
    }

    public static ErrorMessage decodeAfterHeader(ByteBuf buffer, Charset charset, int capabilities) {
        // https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html

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
