package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.common.ResultSet;
import com.xs0.asyncdb.mysql.message.server.*;
import io.netty.channel.ChannelHandlerContext;

public interface MySQLHandlerDelegate {
    void onHandshake(HandshakeMessage message);
    void onError(ErrorMessage message);
    void onOk(OkMessage message);
    void onEOF(EOFMessage message);
    void exceptionCaught(Throwable exception);
    void connected(ChannelHandlerContext ctx);
    void onResultSet(ResultSet resultSet, EOFMessage message);
    void switchAuthentication(AuthenticationSwitchRequest message);
}
