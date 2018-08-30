package com.xs0.asyncdb.mysql;

import com.xs0.asyncdb.common.Configuration;
import com.xs0.asyncdb.common.Connection;
import com.xs0.asyncdb.common.pool.TimeoutScheduler;
import com.xs0.asyncdb.common.util.Version;
import com.xs0.asyncdb.mysql.codec.MySQLHandlerDelegate;
import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public abstract class MySQLConnection extends TimeoutScheduler implements MySQLHandlerDelegate, Connection {
    private static AtomicLong counter = new AtomicLong();
    private static Version microsecondsVersion = new Version(5, 6, 0);
    private static Logger log = LoggerFactory.getLogger(MySQLConnection.class);

    public MySQLConnection(Configuration configuration,
                           CharsetMapper charsetMapper,
                           EventLoopGroup group
                           ) {

    }

}
